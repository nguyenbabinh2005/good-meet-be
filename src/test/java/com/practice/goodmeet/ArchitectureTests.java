package com.practice.goodmeet;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.CacheMode;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.practice.goodmeet", cacheMode = CacheMode.PER_CLASS)
class ArchitectureTests {

  @ArchTest
  static final ArchRule DOMAIN_IS_FRAMEWORK_INDEPENDENT =
      noClasses()
          .that()
          .resideInAPackage("..modules..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..",
              "jakarta.persistence..",
              "org.hibernate..",
              "..modules..api..",
              "..modules..application..",
              "..modules..infrastructure..");

  @ArchTest
  static final ArchRule APPLICATION_DOES_NOT_DEPEND_ON_OUTER_MODULE_LAYERS =
      noClasses()
          .that()
          .resideInAPackage("..modules..application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..modules..api..",
              "..modules..infrastructure..",
              "jakarta.persistence..",
              "org.hibernate..");

  @ArchTest
  static final ArchRule API_DOES_NOT_DEPEND_ON_INFRASTRUCTURE =
      noClasses()
          .that()
          .resideInAPackage("..modules..api..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..modules..infrastructure..");
}
