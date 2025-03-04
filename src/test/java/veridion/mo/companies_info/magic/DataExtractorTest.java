package veridion.mo.companies_info.magic;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import veridion.mo.companies_info.entities.Company;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DataExtractorTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private DataExtractor dataExtractor;

    @Mock
    private Connection connection;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExtractPhoneNumber() {
        String html = "<html><body><a href='tel:+1234567890'>Call us</a></body></html>";
        Document doc = Jsoup.parse(html);

        String phoneNumber = DataExtractor.extractPhoneNumber(doc);

        assertEquals("+1234567890", phoneNumber);
    }

    @Test
    public void testExtractSocialMediaLinks() {
        String html = "<html><body><a href='https://facebook.com/example'>Facebook</a></body></html>";
        Document doc = Jsoup.parse(html);

        List<String> socialMediaLinks = DataExtractor.extractSocialMediaLinks(doc);

        assertNotNull(socialMediaLinks);
        assertFalse(socialMediaLinks.isEmpty());
        assertTrue(socialMediaLinks.contains("https://facebook.com/example"));
    }

    @Test
    public void testMergeData() {
        Company company1 = new Company("example1.com", "Example 1", "Example Legal 1", new HashSet<>(Arrays.asList("Example 1")), null, new HashSet<>());

        Company company2 = new Company("example1.com", "Example 1", "Example Legal 1", new HashSet<>(Arrays.asList("Example 1")));
        company2.setPhone("098-765-4321");
        company2.setSocialMediaLink(new HashSet<>(Arrays.asList("https://twitter.com/example1")));

        List<Company> extractedData = Arrays.asList(company2);
        List<Company> existingData = Arrays.asList(company1);

        List<Company> mergedData = DataExtractor.mergeData(extractedData, existingData);

        assertNotNull(mergedData);
        assertEquals(1, mergedData.size());
        assertEquals("098-765-4321", mergedData.get(0).getPhone());
        assertTrue(mergedData.get(0).getSocialMediaLink().contains("https://twitter.com/example1"));
    }

    @Test
    public void testLoadCompanies() throws IOException {
        List<Company> companies = dataExtractor.loadCompanies("src/test/resources/companies.csv");

        assertNotNull(companies);
        assertFalse(companies.isEmpty());
        assertEquals("veridiumid.com", companies.get(0).getDomain());
    }

    @Test
    public void testSaveDataIntoElasticsearch() throws IOException {
        List<Company> companies = new ArrayList<>();
        companies.add(new Company("example.com", "Example Company", "Example Legal Name", new HashSet<>(Arrays.asList("Example 1"))));

        doReturn(new Iterable<Object>() {
            @Override
            public Iterator<Object> iterator() {
                return new Iterator<Object>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public Object next() {
                        return null;
                    }
                };
            }
        }).when(elasticsearchOperations).save(anyList(), any(IndexCoordinates.class));

        dataExtractor.saveDataIntoElasticsearch(companies);

        verify(elasticsearchOperations, times(1)).save(companies, IndexCoordinates.of("companies"));
    }
}



