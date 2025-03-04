package veridion.mo.companies_info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import veridion.mo.companies_info.magic.DataExtractor;

@SpringBootApplication
public class CompaniesInfoApplication {

	@Autowired
	DataExtractor dataExtractor;

	public static void main(String[] args)  {

		SpringApplication.run(CompaniesInfoApplication.class, args);

	}

}
