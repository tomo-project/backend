package com.tomo.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

  private static final JavaClasses CLASSES =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages("com.tomo");

  @Test
  @DisplayName("연동 모듈은 실행 앱의 클래스에 의존하지 않는다")
  void integrationsDoNotDependOnApplication() {
    // then
    noClasses()
        .that()
        .resideInAPackage("com.tomo.integration..")
        .should()
        .dependOnClassesThat(
            resideInAPackage("com.tomo..").and(not(resideInAPackage("com.tomo.integration.."))))
        .allowEmptyShould(true)
        .check(CLASSES);
  }

  @Test
  @DisplayName("Controller는 Repository에 직접 의존하지 않는다")
  void controllersDoNotDependOnRepositories() {
    // then
    noClasses()
        .that()
        .resideInAPackage("..controller..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..repository..")
        .allowEmptyShould(true)
        .check(CLASSES);
  }

  @Test
  @DisplayName("Service와 Entity는 HTTP DTO와 Controller에 의존하지 않는다")
  void businessCodeDoesNotDependOnWebTypes() {
    // then
    noClasses()
        .that()
        .resideInAnyPackage("..service..", "..entity..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..dto..", "..controller..")
        .allowEmptyShould(true)
        .check(CLASSES);
  }

  @Test
  @DisplayName("Facade는 Repository에 직접 의존하지 않는다")
  void facadesDoNotDependOnRepositories() {
    // then
    noClasses()
        .that()
        .haveSimpleNameEndingWith("Facade")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..repository..")
        .allowEmptyShould(true)
        .check(CLASSES);
  }

  @Test
  @DisplayName("기능 패키지 사이에 순환 의존이 없다")
  void featurePackagesHaveNoCycles() {
    // then
    slices()
        .matching("com.tomo.(*)..")
        .should()
        .beFreeOfCycles()
        .allowEmptyShould(true)
        .check(CLASSES);
  }
}
