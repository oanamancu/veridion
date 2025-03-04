package veridion.mo.companies_info.magic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import veridion.mo.companies_info.entities.Company;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Component
public class DataExtractor {

    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public DataExtractor(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public static List<Company> extractData(List<String> websites) {

        List<Company> companies = new ArrayList<>();

        LatestAnalysis.DATAPOINTS_COUNT = 0;
        LatestAnalysis.WEBSITES_COUNT = 0;

        for (String website : websites) {
            Company company = new Company();
            company.setDomain(website);

            try {

                // Crawl the main page and internal links
                Set<String> crawledUrls = new HashSet<>();
                String url = "https://" + website;

                crawlPage("https://" + website, company, crawledUrls);

                companies.add(company);

                LatestAnalysis.WEBSITES_COUNT++;

            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                companies.add(company);
            }
        }
        return companies;
    }

    public static void crawlPage(String url, Company company, Set<String> crawledUrls) throws IOException {
        if (crawledUrls.contains(url) || (company.getPhone() != null && !company.getSocialMediaLink().isEmpty())) {
            return;
        }
        crawledUrls.add(url);

        // Fetch the webpage content using Jsoup
        Document doc = Jsoup.connect(url).get();

        // Extract phone number
        String phone = extractPhoneNumber(doc);
        if (!phone.isEmpty() && company.getPhone() == null) {
            company.setPhone(phone);
            LatestAnalysis.DATAPOINTS_COUNT++;
        }

        // Extract social media links
        List<String> socialMediaLinks = extractSocialMediaLinks(doc);
        if(!socialMediaLinks.isEmpty()) {

            LatestAnalysis.DATAPOINTS_COUNT += (int) socialMediaLinks.stream().filter(link -> !company.getSocialMediaLink().contains(link)).count();

            company.getSocialMediaLink().addAll(socialMediaLinks);
        }

        if (company.getPhone() != null && !company.getSocialMediaLink().isEmpty()) {
            return;
        }

        // Find and crawl internal links
        Elements links = doc.select("a");

        for (Element link : links) {
            String href = link.attr("href");
            if (href.startsWith("http")) {
                crawlPage(href, company, crawledUrls);
            }
        }
    }

    public static String extractPhoneNumber(Document doc) {
        // Look for phone numbers in "tel" links
        Elements phoneElements = doc.select("a[href^=tel]");
        for (Element phoneElement : phoneElements) {
            return phoneElement.attr("href").replace("tel:", "");
        }

        // Look for sequences of digits that match phone number patterns
        Pattern phonePattern = Pattern.compile("\\(?\\b\\d{3}[)-.\\s]*\\d{3}[-.\\s]*\\d{4}\\b");
        Matcher matcher = phonePattern.matcher(doc.text());
        if (matcher.find()) {
            return matcher.group();
        }

        return "";
    }

    public static List<String> extractSocialMediaLinks(Document doc) {
        List<String> socialMediaLinks = new ArrayList<>();
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String href = link.attr("href");
            if (href.contains("facebook.com") || href.contains("twitter.com") || href.contains("instagram.com")) {
                socialMediaLinks.add(href);
            }
        }
        return socialMediaLinks;
    }

    public static List<Company> mergeData(List<Company> extractedData, List<Company> existingData) {
        Map<String, Company> companyMap = existingData.stream()
                .collect(Collectors.toMap(Company::getDomain, company -> company));

        for (Company newCompany : extractedData) {
            Company existingCompany = companyMap.get(newCompany.getDomain());
            if (existingCompany != null) {

                if (!newCompany.getPhone().isEmpty()) {
                    existingCompany.setPhone(newCompany.getPhone());
                }

                existingCompany.getSocialMediaLink().addAll(newCompany.getSocialMediaLink());

            }
        }

        return new ArrayList<>(companyMap.values());
    }

    public static List<String> loadWebsites(String fileName) throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8))
                .lines().skip(1).collect(Collectors.toList());
    }

    public List<Company> loadCompanies(String fileName) throws IOException {
        List<Company> companies = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        reader.readLine(); // Skip header
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            HashSet<String> allNames = parts[3] != null ? new HashSet<>(Arrays.asList(parts[3].split("\\|"))) : new HashSet<>();
            Company company = new Company(parts[0], parts[1], parts[2], allNames);
            company.setPhone(parts.length >= 5 ? parts[4] : "");
            company.setSocialMediaLink(parts.length >= 6 ? new HashSet<>(Arrays.asList(parts[5].split("\\|"))) : new HashSet<>());
            companies.add(company);
        }
        return companies;
    }

    public static void saveDataIntoFile(List<Company> companies, String fileName) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("domain,company_commercial_name,company_legal_name,company_all_available_names,phone,social_media_link");
            for (Company company : companies) {
                writer.printf("%s,%s,%s,%s,%s,%s%n", company.getDomain(), company.getCommercialName(),
                        company.getLegalName(), String.join("|", company.getAllAvailableNames()),
                        company.getPhone(), String.join("|", company.getSocialMediaLink()));
            }
        }
    }

    public void saveDataIntoElasticsearch(List<Company> companies) throws IOException {
        IndexCoordinates indexCoordinates = IndexCoordinates.of("companies");
        elasticsearchOperations.save(companies, indexCoordinates);
    }

}


