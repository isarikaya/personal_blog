package tools

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.data
import play.api.libs.json.Json
import play.api.libs.json.Writes
import slick.driver.MySQLDriver.api._
import entities._
import BlogDb._
import forms._
import java.sql.Connection
import types._
import java.nio.file.Paths
import org.joda.time.DateTime
import java.util.regex.Pattern
import compositions.AuthAction
import transfers.BlogDTO

class SeoTools {
  def seoTitle(name: String) = {
    val sep = '-'
    name.map(_.toLower match {
      case c if (('a' <= c && c <= 'z') || ('0' <= c && c <= '9')) => c
      case 'á' => 'a'
      case 'é' => 'e'
      case 'í' => 'i'
      case 'ó' => 'o'
      case 'ú' => 'u'
      case 'ñ' => 'n'
      case '/' => '-'
      case ' ' => '-'
      case 'ş' => 's'
      case 'Ş' => 'S'
      case 'İ' => 'i'
      case 'I' => 'i'
      case 'ı' => 'i'
      case 'ö' => 'o'
      case 'Ö' => 'O'
      case 'ü' => 'u'
      case 'Ü' => 'U'
      case 'ç' => 'c'
      case 'Ç' => 'c'
      case 'ğ' => 'g'
      case 'Ğ' => 'g'
      case '?' => '-'
      case '.' => '-'
      case ''' => '-'
      case '#' => '-'
      case '%' => '-'
      case '&' => '-'
      case '*' => '-'
      case '@' => '-'
      case '+' => '-'
      case _ => sep
    }).replaceAll(sep + "+", sep + "")
  }
} 