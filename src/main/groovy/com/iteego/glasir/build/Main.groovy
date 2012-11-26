package com.iteego.glasir.build

import org.parboiled.Parboiled
import org.parboiled.support.ParsingResult
import org.parboiled.parserunners.ReportingParseRunner
import org.parboiled.support.ParseTreeUtils

/**
 * Created with IntelliJ IDEA.
 * User: mbjarland
 * Date: 11/26/12
 * Time: 4:43 PM
 * To change this template use File | Settings | File Templates.
 */
class Main {
  static main(args) {
    String data = new File('/work/atg/ATG10.0.2/home/META-INF/MANIFEST.MF').text

    ManifestParser parser = Parboiled.createParser(ManifestParser.class)
    ParsingResult<?> result = ReportingParseRunner.run(parser.ManifestFile(), data)
    String parseTreePrintOut = ParseTreeUtils.printNodeTree(result)

    println parseTreePrintOut

  }
}
