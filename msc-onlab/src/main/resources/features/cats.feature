Feature: cats end-point

  Background:
    * url baseUrl

  Scenario: Search for a term on Google
    Given param q = 'karate framework'
    When method GET
    Then status 200