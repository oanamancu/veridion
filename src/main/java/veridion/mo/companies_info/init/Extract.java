package veridion.mo.companies_info.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import veridion.mo.companies_info.entities.Company;
import veridion.mo.companies_info.magic.DataExtractor;

import java.io.IOException;
import java.util.List;

import static veridion.mo.companies_info.magic.DataExtractor.*;

@Component
@Profile("!test")
public class Extract {
    @Autowired
    DataExtractor dataExtractor;

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() throws IOException {
        System.out.println("hello world, I have just started up");

        List<String> websites = loadWebsites("src/main/resources/static/sample-websites.csv");

        List<Company> extractedData = extractData(websites);

        List<Company> existingData = dataExtractor.loadCompanies("src/main/resources/static/sample-websites-company-names.csv");

        List<Company> mergedData = mergeData(extractedData, existingData);

        dataExtractor.saveDataIntoElasticsearch(mergedData);
    }
}
