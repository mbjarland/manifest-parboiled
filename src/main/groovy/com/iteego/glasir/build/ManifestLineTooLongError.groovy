package com.iteego.glasir.build

import org.parboiled.Context

/**
 * Created with IntelliJ IDEA.
 * User: mbjarland
 * Date: 11/26/12
 * Time: 7:03 PM
 * To change this template use File | Settings | File Templates.
 */
class ManifestLineTooLongError extends ManifestParseError {
  ManifestLineTooLongError(Context context, int index) {
    super(context, index, "manifest line longer than 72 characters")
  }
}
