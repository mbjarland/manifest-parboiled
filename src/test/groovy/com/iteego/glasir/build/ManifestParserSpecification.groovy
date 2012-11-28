/*
 * Copyright (C) 2011 Iteego Inc and Matias Bjarland <mbjarland@gmail.com>
 *
 * This file is part of Glasir, a Gradle build framework for ATG E-Commerce
 * projects created by Iteego Inc and Matias Bjarland.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.iteego.glasir.build

import spock.lang.*
import org.parboiled.Parboiled
import org.parboiled.support.ParsingResult
import org.parboiled.parserunners.ReportingParseRunner
import org.parboiled.support.ParseTreeUtils
import org.parboiled.errors.InvalidInputError
import org.parboiled.errors.ParseError
import groovy.io.LineColumnReader
import org.parboiled.buffers.InputBuffer
import org.parboiled.support.Position
import org.parboiled.parserunners.TracingParseRunner
import org.parboiled.parserunners.ProfilingParseRunner
import org.parboiled.parserunners.ParseRunner

/**
 * http://code.google.com/p/spock/wiki/SpockBasics
 */
class ManifestParserSpecification extends Specification {

  private ParseRunner getParseRunner() {
    def parser = Parboiled.createParser(ManifestParser)
    //def runner = new TracingParseRunner(parser.ManifestFile())//
    //def runner = new ProfilingParseRunner(parser.ManifestFile())//
    new ReportingParseRunner(parser.ManifestFile())
  }

  private printErrors(ParsingResult result) {
    result.parseErrors.each { ParseError e ->
      Position start = e.inputBuffer.getPosition(e.startIndex)
      Position end = e.inputBuffer.getPosition(e.endIndex)
      String failure = e.inputBuffer.extract(e.startIndex, e.endIndex)

      println "${e.toString()}"
      println "TYPE:  ${e.class.name}"
      println "AT:    [${start.line}, ${start.column}] to [${end.line}, ${end.column}]"
      println "DATA:  '${failure}'"
      println "ERROR: start: ${e.startIndex}, end: ${e.endIndex}, msg: ${e.errorMessage}"
    }
  }

  File getSampleManifest() {
    URL url = this.class.classLoader.getResource('MANIFEST.MF')
    assert url != null, "Could not locate file MANIFEST.MF on the test classpath"

    return new File(url.toURI())
  }

  def "should parse large manifest file"() {
    given:
      String data = sampleManifest.text

    when:
      ParsingResult<?> result = parseRunner.run(data)
      printErrors(result)
      //String parseTreePrintOut = ParseTreeUtils.printNodeTree(result)

      //println parseTreePrintOut

    then:
      result.parseErrors == []
      result.resultValue == null
  }

  def "should fail on lines longer than 72 bytes"() {
    given:
      String data
      ParsingResult<?> result

    when:
      data = """|Manifest-Version: 1.0
                |SomeAttribute: SomeValue
                |TooLongAttribute: 9212345678931234567894123456789512345678961234567897
                |
                |""".stripMargin()

      result = parseRunner.run(data)
      printErrors(result)

    then:
      result.parseErrors == []

    when:
       data = """|Manifest-Version: 1.0
                 |SomeAttribute: SomeValue
                 |TooLongAttribute: 92123456789312345678941234567895123456789612345678971
                 |
                 |""".stripMargin()

        result = parseRunner.run(data)

    then:
      result.parseErrors.first() instanceof ManifestLineTooLongError

  }

  @IgnoreRest
  def "should fail on Name attribute name in main section"() {
    given:
      String data = """|Manifest-Version: 1.0
                       |Name: SomeValue
                       |SomeOtherAttribute: SomeValue
                       |
                       |""".stripMargin()

    when:
      ParsingResult<?> result = parseRunner.run(data)

    then:
      result.parseErrors.size() == 1
  }

}
