package com.rxlogix

import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import org.apache.commons.codec.digest.DigestUtils
import org.passay.CharacterRule
import org.passay.EnglishCharacterData
import org.passay.PasswordData
import org.passay.PasswordGenerator
import org.passay.PasswordValidator
import org.passay.Rule
import org.passay.RuleResult
import org.passay.CharacterData
import org.springframework.context.MessageSource


import java.security.InvalidParameterException

class PasswordService {
    MessageSource messageSource
    SpringSecurityService springSecurityService

    String generate() {
        Integer minLength = Holders.config.getProperty('password.minLength', Integer)

        List<Rule> rules = buildPasswordRules()
        PasswordGenerator generator = new PasswordGenerator()
        Integer passwordLength = minLength
        generator.generatePassword(passwordLength, rules as List<CharacterRule>)
    }

    String generateDefaultPassword(){
        return Holders.config.getProperty('password.defaultUserPassword','changeit')
    }

    Tuple2<Boolean, List<String>> validatePassword(String password) {
        Integer minLength = Holders.config.getProperty('password.minLength', Integer)
        Integer maxLength = Holders.config.getProperty('password.maxLength', Integer)
        Integer minNumOfUpperCaseChars = Holders.config.getProperty('password.minNumOfUpperCaseChars', Integer)
        Integer minNumOfLowerCaseChars = Holders.config.getProperty('password.minNumOfLowerCaseChars', Integer)
        Integer minNumOfSpecialChars = Holders.config.getProperty('password.minNumOfSpecialChars', Integer)
        Integer minNumOfDigits = Holders.config.getProperty('password.minNumOfDigits', Integer)
        Integer minMumOfAlphabets = Holders.config.getProperty('password.minNumOfAlphabets', Integer)
        String specialChars = Holders.config.getProperty('password.specialChars')

        if (password) {
            List<String> msgs = []

            String msg = null
            if (password.length() < minLength) {
                msg = messageSource.getMessage('message.password.too.short', [minLength] as Object[], Locale.default)
            } else if (password.length() > maxLength) {
                msg = messageSource.getMessage('message.password.too.long', [maxLength] as Object[], Locale.default)
            }
            if (msg) msgs.add msg

            List<CharacterRule> rules = buildPasswordRules()
            PasswordValidator passwordValidator = new PasswordValidator(rules)
            RuleResult result = passwordValidator.validate(new PasswordData(password))

            if (result.isValid() && !msgs) {
                return new Tuple2<Boolean, List<String>>(true, [])
            } else {
                msgs.addAll(result.details.collect { resultItem ->
                    String errorCode = resultItem.getErrorCode()

                    switch (errorCode) {
                        case 'INSUFFICIENT_ALPHABETICAL':
                            messageSource.getMessage('message.password.insufficient.alphabets',
                                    [minMumOfAlphabets] as Object[], Locale.default)
                            break;
                        case 'INSUFFICIENT_UPPERCASE':
                            messageSource.getMessage('message.password.insufficient.upperCase',
                                    [minNumOfUpperCaseChars] as Object[], Locale.getDefault())
                            break
                        case 'INSUFFICIENT_LOWERCASE':
                            messageSource.getMessage('message.password.insufficient.lowerCase',
                                    [minNumOfLowerCaseChars] as Object[], Locale.getDefault())
                            break
                        case 'INSUFFICIENT_DIGIT':
                            messageSource.getMessage('message.password.insufficient.digits',
                                    [minNumOfDigits] as Object[], Locale.default)
                            break
                        case 'INSUFFICIENT_SPECIAL':
                            messageSource.getMessage('message.password.insufficient.special.chars',
                                    [minNumOfSpecialChars, specialChars] as Object[], Locale.default)
                            break;
                        default:
                            'Invalid password'
                            break;
                    }
                })
                return new Tuple2<Boolean, List<String>>(false, msgs)
            }
        } else {
            throw new InvalidParameterException("Password can not be null")
        }
    }

    Boolean isPasswordRepeated(String username, String plainPassword) {
        User user = User.findByUsernameIgnoreCase(username)
        def oldPasswords = user.passwordDigests
        if (plainPassword) {
            def encodedPassword = this.digestPassword(plainPassword)
            if (oldPasswords) {
                boolean contains = oldPasswords.contains(encodedPassword)

                return contains
            }
            else
                false
        } else {
            false
        }
    }

    List<CharacterRule> buildPasswordRules() {
        Integer minNumOfUpperCaseChars = Holders.config.getProperty('password.minNumOfUpperCaseChars', Integer)
        Integer minNumOfLowerCaseChars = Holders.config.getProperty('password.minNumOfLowerCaseChars', Integer)
        Integer minNumOfSpecialChars = Holders.config.getProperty('password.minNumOfSpecialChars', Integer)
        Integer minMumOfAlphabets = Holders.config.getProperty('password.minNumOfAlphabets', Integer)
        Integer minNumOfDigits = Holders.config.getProperty('password.minNumOfDigits', Integer)
        String specialChars = Holders.config.getProperty('password.specialChars')

        CharacterRule specialCharsRule = new CharacterRule(new CharacterData() {
            @Override
            String getErrorCode() { "INSUFFICIENT_SPECIAL" }

            @Override
            String getCharacters() { specialChars }
        }, minNumOfSpecialChars)
        CharacterRule alphabetsRule = new CharacterRule(EnglishCharacterData.Alphabetical, minMumOfAlphabets)
        CharacterRule digitsRule = new CharacterRule(EnglishCharacterData.Digit, minNumOfDigits)
        CharacterRule upperCaseRule = new CharacterRule(EnglishCharacterData.UpperCase, minNumOfUpperCaseChars)
        CharacterRule lowerCaseRule = new CharacterRule(EnglishCharacterData.LowerCase, minNumOfLowerCaseChars)
        [upperCaseRule, lowerCaseRule, digitsRule, specialCharsRule, alphabetsRule]
    }

    String digestPassword(String password) {
        DigestUtils.md5Hex(password).toUpperCase()
    }
}
