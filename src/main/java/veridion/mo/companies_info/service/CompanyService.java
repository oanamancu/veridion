package veridion.mo.companies_info.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import veridion.mo.companies_info.dto.CompanyDTO;
import veridion.mo.companies_info.entities.Company;
import veridion.mo.companies_info.magic.DataExtractor;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
public class CompanyService {

    private final DataExtractor dataExtractor;
    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public CompanyService(DataExtractor dataExtractor, ElasticsearchOperations elasticsearchOperations) {
        this.dataExtractor = dataExtractor;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public Optional<Company> findBestMatchingCompanyInFile(CompanyDTO query) {
        try {
            List<Company> companies = dataExtractor.loadCompanies("src/main/resources/static/merged-companies.csv");
            return companies.stream()
                    .filter(company -> company.matches(query) > 0)
                    .max(Comparator.comparingInt(x -> x.matches(query)));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }


    public Optional<Company> findBestMatchingCompanyInElasticSearch(CompanyDTO query) {
        Criteria criteria = new Criteria();

        if (query.getName() != null && !query.getName().isEmpty()) {
            criteria = criteria.and(new Criteria("allAvailableNames").contains(query.getName()));
        }
        if (query.getWebsite() != null && !query.getWebsite().isEmpty()) {
            criteria = criteria.and(new Criteria("domain").is(query.getWebsite()));
        }
        if (query.getPhone() != null && !query.getPhone().isEmpty()) {
            criteria = criteria.and(new Criteria("phone").is(query.getPhone()));
        }
        if (query.getFacebookProfile() != null && !query.getFacebookProfile().isEmpty()) {
            criteria = criteria.and(new Criteria("socialMediaLink").contains(query.getFacebookProfile()));
        }

        Query searchQuery = new CriteriaQuery(criteria);

        SearchHits<Company> searchHits = elasticsearchOperations.search(searchQuery, Company.class);

        List<Company> companies = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        return companies.stream().max(Comparator.comparingInt(company -> company.matches(query)));
    }
}
