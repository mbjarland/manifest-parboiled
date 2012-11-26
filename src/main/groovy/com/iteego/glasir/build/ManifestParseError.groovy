package com.iteego.glasir.build

import org.parboiled.Context
import org.parboiled.errors.InvalidInputError
import org.parboiled.support.Position
import org.parboiled.support.MatcherPath

/**
 * Created with IntelliJ IDEA.
 * User: mbjarland
 * Date: 11/26/12
 * Time: 7:02 PM
 */
abstract class ManifestParseError extends InvalidInputError {
  String message

  ManifestParseError(Context context, int index, String message) {
    super(context.inputBuffer, index, [context.path], '')

    this.message = message
  }

  String toString() {
    Position p = inputBuffer.getPosition(startIndex)
    "$message encountered at [${p.line}, ${p.column}]"
  }
}
