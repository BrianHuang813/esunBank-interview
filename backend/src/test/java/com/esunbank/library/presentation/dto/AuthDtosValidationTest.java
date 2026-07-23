package com.esunbank.library.presentation.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthDtosValidationTest {

    private static jakarta.validation.ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void registerAcceptsTwentyCharacterPassword() {
        var request = new AuthDtos.RegisterRequest(
                "0912345678",
                "a".repeat(20),
                "王小明"
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void registerRejectsTwentyOneCharacterPassword() {
        var request = new AuthDtos.RegisterRequest(
                "0912345678",
                "a".repeat(21),
                "王小明"
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("password");
    }

    @Test
    void loginRejectsTwentyOneCharacterPassword() {
        var request = new AuthDtos.LoginRequest(
                "0912345678",
                "a".repeat(21)
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("password");
    }

    @Test
    void registerAcceptsThirtyCharacterUserName() {
        var request = new AuthDtos.RegisterRequest(
                "0912345678",
                "Interview123!",
                "a".repeat(30)
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void registerRejectsThirtyOneCharacterUserName() {
        var request = new AuthDtos.RegisterRequest(
                "0912345678",
                "Interview123!",
                "a".repeat(31)
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("userName");
    }

    @Test
    void registerAcceptsChineseAndEnglishUserNames() {
        var chineseRequest = new AuthDtos.RegisterRequest(
                "0912345678",
                "Interview123!",
                "王小明"
        );
        var englishRequest = new AuthDtos.RegisterRequest(
                "0912345678",
                "Interview123!",
                "John Smith"
        );

        assertThat(validator.validate(chineseRequest)).isEmpty();
        assertThat(validator.validate(englishRequest)).isEmpty();
    }

    @Test
    void registerRejectsNumbersAndSpecialCharactersInUserName() {
        var numberRequest = new AuthDtos.RegisterRequest(
                "0912345678",
                "Interview123!",
                "王小明123"
        );
        var symbolRequest = new AuthDtos.RegisterRequest(
                "0912345678",
                "Interview123!",
                "John@Smith"
        );

        assertThat(validator.validate(numberRequest))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("userName");
        assertThat(validator.validate(symbolRequest))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("userName");
    }
}
