package veridion.mo.companies_info.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import veridion.mo.companies_info.dto.CompanyDTO;
import veridion.mo.companies_info.entities.Company;
import veridion.mo.companies_info.magic.LatestAnalysis;
import veridion.mo.companies_info.service.CompanyService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }


    @GetMapping("/search")
    @ResponseBody
    public Optional<Company> getCompany(@RequestBody CompanyDTO query) {
        return companyService.findBestMatchingCompanyInElasticSearch(query);
    }

    @GetMapping("/latestAnalysis")
    @ResponseBody
    public Map<String, Integer> getAnalysis() {
        return new HashMap<String, Integer>() {{
            put("datapoints", LatestAnalysis.DATAPOINTS_COUNT);
            put("websites", LatestAnalysis.WEBSITES_COUNT);
        }};
    }

}
