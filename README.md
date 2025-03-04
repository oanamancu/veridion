# Companies Information Matcher

!!! never used the list with the domains provided since from what I remember not all of them were secure nor the file for testing the app since there
are now a lot of fake interviews and some oof them ask you for projects that also implies malicious code from them like:
- https://www.reddit.com/r/programare/comments/1hec513/psa_atentie_la_interviuri_scam/

So I never tested this properly with more than 2 or 3 links. Also I do not really know the low about scrapping public pages.

## Project Description

The Companies Information Matcher is a Java-based application designed to extract, merge, and match company information from various websites. This project leverages web scraping, data extraction, and a matching algorithm to provide a high match rate against stored company profiles.

## Set Up
You can run docker-compose file for elasticsearch: docker up --build

## Access app

- the web scrapping part will begin automatically when the app starts. Put the faloowing files into src/main/resources/static:
    - sample-websites.csv
    - sample-websites-company-names.csv

- REST API Base URL: http://localhost:8080/api/companies
  - Search for Best Matching Company
    Endpoint: /api/companies/search
  
    Method: POST

    Request Body: json

        {
            "name": "Example Company",
  
            "website": "example.com",
  
            "phone": "123-456-7890",
  
            "facebookProfile": "https://facebook.com/example"
        }


    Response: Returns the best matching company profile.
- localhost:8080/api/companies/latestAnalysis

## Approach

1) started with company class. it's java it's oop we always end up with objects and since this project is about companies' data it's abwios that we need a company class.
   2) read the websites and put them in a list so I can use them later for what may come
  3) same with the companies I already have
    4) searched the websites for phone number and social links used regular expressions. Created a new company object for each domain.
    5) merged lists from 3) and 4)
    6) added at 4) the logic for the analysis part
  7) added an endpoint for 6
  8) saved 5) to a new csv file
  9) re-read project to dos discovered I should use elasticsearch:
    - created docker-compose for elasticsearch
    - re-do 8 to save to elasticsearch
  10) implement search function and add endpoint to it. Create a score for each entry that matches. For every data that matches (name, website, phone number and facebook profile) they receive an extrapoint. Something simple for a poc project and quite logically
  11) write some unit tests
  12) write some doc
    13) read that I also have to write this (my approach)
  14) put it on github

