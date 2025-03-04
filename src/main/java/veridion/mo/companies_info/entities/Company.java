package veridion.mo.companies_info.entities;

import veridion.mo.companies_info.dto.CompanyDTO;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


import java.util.HashSet;

@Document(indexName = "companies")
public class Company {

    @Id
    private String domain;
    private String commercialName;
    private String legalName;
    @Field(type = FieldType.Text)
    private HashSet<String> allAvailableNames;
    private String phone;
    @Field(type = FieldType.Text)
    private HashSet<String> socialMediaLink;

    public Company(String domain, String phone, HashSet<String> socialMediaLink) {
        this.domain = domain;
        this.phone = phone;
        this.socialMediaLink = socialMediaLink;
    }

    public Company(String domain, String commercialName, String legalName, HashSet<String> allAvailableNames) {
        this.domain = domain;
        this.commercialName = commercialName;
        this.legalName = legalName;
        this.allAvailableNames = allAvailableNames;
    }


    public Company(String domain, String commercialName, String legalName, HashSet<String> allAvailableNames, String phone, HashSet<String> socialMediaLink) {
        this.domain = domain;
        this.commercialName = commercialName;
        this.legalName = legalName;
        this.allAvailableNames = allAvailableNames;
        this.phone = phone;
        this.socialMediaLink = socialMediaLink;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getCommercialName() {
        return commercialName;
    }

    public void setCommercialName(String commercialName) {
        this.commercialName = commercialName;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public HashSet<String> getAllAvailableNames() {
        return allAvailableNames;
    }

    public void setAllAvailableNames(HashSet<String> allAvailableNames) {
        this.allAvailableNames = allAvailableNames;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public HashSet<String> getSocialMediaLink() {
        return socialMediaLink;
    }

    public void setSocialMediaLink(HashSet<String> socialMediaLink) {
        this.socialMediaLink = socialMediaLink;
    }

    public Company() {
        this.allAvailableNames = new HashSet<>();
        this.socialMediaLink = new HashSet<>();
    }

    @Override
    public String toString() {
        return "Company{" +
                "domain='" + domain + '\'' +
                ", commercialName='" + commercialName + '\'' +
                ", legalName='" + legalName + '\'' +
                ", allAvailableNames=" + allAvailableNames +
                ", phone='" + phone + '\'' +
                ", socialMediaLink='" + socialMediaLink + '\'' +
                '}';
    }



    public int matches(CompanyDTO companyDTO) {
         int score = 0;

         if (allAvailableNames.stream().anyMatch(name -> name.equalsIgnoreCase(companyDTO.getName()))) score++;
         if (companyDTO.getWebsite() != null && companyDTO.getWebsite().equalsIgnoreCase(domain)) score++;
         if (companyDTO.getPhone() != null && companyDTO.getPhone().equalsIgnoreCase(phone)) score++;
         if (socialMediaLink.contains(companyDTO.getFacebookProfile())) score++;

         return score;
    }

}
