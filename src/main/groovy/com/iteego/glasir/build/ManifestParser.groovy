package com.iteego.glasir.build

import org.parboiled.annotations.BuildParseTree
import org.parboiled.BaseParser
import org.parboiled.Rule
import org.parboiled.Action
import org.parboiled.Context

/**
 section:                       *header +newline
 nonempty-section:      +header +newline
 newline:                      CR LF | LF | CR (not followed by LF)
 header:                       name : value
 name:                         alphanum *headerchar
 value:                          SPACE *otherchar newline *continuation
 continuation:              SPACE *otherchar newline
 alphanum:                  {A-Z} | {a-z} | {0-9}
 headerchar:                alphanum | - | _
 otherchar:                  any UTF-8 character except NUL, CR and LF
 ; Also: To prevent mangling of files sent via straight e-mail, no
 ; header will start with the four letters "From".


 manifest-file:                    main-section newline *individual-section
 main-section:                    version-info newline *main-attribute
 version-info:                      Manifest-Version : version-number
 version-number :               digit+{.digit+}*
 main-attribute:                 (any legitimate main attribute) newline
 individual-section:             Name : value newline *perentry-attribute
 perentry-attribute:            (any legitimate perentry attribute) newline
 newline :                            CR LF | LF | CR (not followed by LF)
 digit:                                {0-9}


 */

@BuildParseTree
class ManifestParser extends BaseParser implements GroovyObject {
  Action pushCurrentIndex = { Context c ->
    c.valueStack.push(c.currentIndex);
    true
  } as Action

  Action popAndReportLineTooLong = { Context c ->
    Integer indexBeforeNewLine = c.valueStack.pop() as Integer
    if (indexBeforeNewLine - lastNewLinePosition > 72) {
      c.parseErrors << new ManifestLineTooLongError(c, indexBeforeNewLine)
    }
    lastNewLinePosition = c.currentIndex
    true
  } as Action

  long lastNewLinePosition = 0

  /**
   * manifest-file:                    main-section newline *individual-section
   *
   * @return
   */
  Rule ManifestFile() {
    Sequence(MainSection(), NewLine(), ZeroOrMore(IndividualSection()))
  }

  /**
   * main-section:                    version-info newline *main-attribute
   *
   * @return
   */
  Rule MainSection() {
    Sequence(VersionInfo(), NewLine(), ZeroOrMore(MainAttribute()))
  }

  /**
   * version-info:                      Manifest-Version : version-number
   *
   * @return
   */
  Rule VersionInfo() {
    Sequence('Manifest-Version: ', VersionNumber())
  }

  /**
   * version-number :               digit+{.digit+}*
   *
   * @return
   */
  Rule VersionNumber() {
    Sequence(Number(), ZeroOrMore(Sequence('.', Number())))
  }

  /**
   * main-attribute:                 (any legitimate main attribute) newline
   *
   * @return
   */
  Rule MainAttribute() {
    Sequence(MainAttributeName(), ':', Value())
  }

  Rule MainAttributeName() {
    TestNot('Name')
    AttributeName()
  }

  /**
   *  individual-section:             Name : value newline *perentry-attribute
   *
   * @return
   */
  Rule IndividualSection() {
    Sequence(IndividualSectionHeader(), ZeroOrMore(PerEntryAttribute()))
  }

  Rule IndividualSectionHeader() {
    Sequence('Name: ', IndividualSectionName())
  }

  Rule IndividualSectionName() {
    Sequence(OneOrMore(OtherChar()), NewLine())
  }

  /**
   * perentry-attribute:            (any legitimate perentry attribute) newline
   *
   * @return
   */
  Rule PerEntryAttribute() {
    Sequence(PerEntryAttributeName(), ':', Value())
  }

  Rule PerEntryAttributeName() {
    TestNot('Name')
    AttributeName()
  }

  Rule AttributeName() {
    Sequence(AlphaNum(), ZeroOrMore(HeaderChar()))
  }

  /**
   * value:                          SPACE *otherchar newline *continuation
   *
   * Note: we have moved the SPACE from here into the calling (PerEntryAttribute and
   * MainAttribute) rules as this gives us a clean representation of the
   * value of an attribute without trimming off the leading space when looking
   * at the resulting parse tree.
   *
   * @return
   */
  Rule Value() {
    Sequence(' ', ZeroOrMore(OtherChar()), NewLine(), ZeroOrMore(Continuation()))
  }

  /**
   * continuation:              SPACE *otherchar newline
   * @return
   */
  Rule Continuation() {
    Sequence(' ', ZeroOrMore(OtherChar()), NewLine())
  }

  /**
   * headerchar:                alphanum | - | _
   * @return
   */
  Rule HeaderChar() {
    FirstOf(AlphaNum(), AnyOf("_-"))
  }

  /**
   * otherchar:                 any UTF-8 character except NUL, CR and LF
   * @return
   */
  Rule OtherChar() {
    NoneOf("\n\r")
  }

  /**
   * newline :                            CR LF | LF | CR (not followed by LF)
   * @return
   */
  Rule NewLine() {
    Sequence (pushCurrentIndex, FirstOf('\n', Sequence('\r', Optional('\n'))), popAndReportLineTooLong)
  }

  Rule Number() {
    OneOrMore(Digit())
  }

  /**
   * digit:                                {0-9}
   * @return
   */
  Rule Digit() {
    CharRange('0' as char, '9' as char)
  }

  /**
   * alphanum:                  {A-Z} | {a-z} | {0-9}
   * @return
   */
  Rule AlphaNum() {
    FirstOf(Digit(), CharRange('a' as char, 'z' as char), CharRange('A' as char, 'Z' as char))
  }

}