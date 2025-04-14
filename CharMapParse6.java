/*
  Character Map Parse #6 - Quick-and-Dirty Extraction of Unicode Data
  Written by: Keith Fenske, http://kwfenske.github.io/
  Monday, 14 November 2016
  Java class name: CharMapParse6
  Copyright (c) 2016 by Keith Fenske.  Apache License or GNU GPL.

  This is a quick-and-dirty Java 1.4 console application to parse three data
  files with Unicode character numbers (hexadecimal) and names.  These files
  are subject to change each time the Unicode standard is revised, both in
  content and in syntax:

   1. UnicodeData.txt (standard character names)
      from: http://www.unicode.org/Public/UNIDATA/UnicodeData.txt

   2. Unihan_Readings.txt and Unihan_Variants.txt (Chinese-Japanese-Korean)
      from: http://www.unicode.org/Public/UNIDATA/Unihan.zip

  There are no parameters.  The input data files must be in the current working
  directory.  Output goes into a file called "parsed-unidata.txt" as US-ASCII
  plain text (or encoded as UTF-8) for the "CharMap4.txt" data file used by the
  CharMap4 Java application.  Some manual editing will be required:

   1. The data goes after the explanatory comments in the CharMap4.txt file.

   2. Control codes (U+0000 to U+001F and U+007F to U+009F) will have the wrong
      descriptions.  You should use the existing descriptions that were created
      by hand.  Search for "<" and ">" in the output file.

   3. There will be spurious entries for the first and last characters in large
      Unicode blocks (ranges).  Again, search for "<" and ">".

   4. Some descriptions will have incorrect capitalization.  In particular, you
      may find "Apl" instead of the correct "APL" (U+2336 to U+2395), and "Cjk"
      instead of the correct "CJK" (U+2E80 to U+31E3).

   5. Some "CJK compatibility ideograph" from U+FA0E to U+FAD9 and U+2F800 to
      U+2FA1D have default names in UnicodeData.txt that are not replaced by
      better information in the Unihan files.  See also CharMapParse7.

  Modified for Unicode 9.0.0 (2016) and still working on Unicode 11.0.0 (2018).
  CharMapParse6 replaces four older programs numbered 2 to 5.  (CharMapParse1
  does a different but related job.)  This source file should only be
  distributed with the source for CharMap4.  General users have no need for the
  CharMapParse6 application.  THIS CODE IS UGLY AND SHOULD *NOT* BE USED AS THE
  BASIS FOR ANY OTHER PROGRAMS.

  Apache License or GNU General Public License
  --------------------------------------------
  CharMapParse6 is free software and has been released under the terms and
  conditions of the Apache License (version 2.0 or later) and/or the GNU
  General Public License (GPL, version 2 or later).  This program is
  distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY,
  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE.  See the license(s) for more details.  You should have
  received a copy of the licenses along with this program.  If not, see the
  http://www.apache.org/licenses/ and http://www.gnu.org/licenses/ web pages.
*/

import java.io.*;                 // standard I/O
import java.util.*;               // calendars, dates, lists, maps, vectors
import java.util.regex.*;         // regular expressions

public class CharMapParse6
{
  /* constants */

  static final String EMPTY = ""; // the empty string
  static final String VARIANT_TAG = "Z:"; // sort variants after other tags

  /* The following string arrays are used when converting Unicode character
  numbers for Korean Hangul syllables to caption strings.  Please refer to
  these on-line references:

      http://en.wikipedia.org/wiki/Hangul
      http://en.wikipedia.org/wiki/Korean_language_and_computers
      http://en.wikipedia.org/wiki/Korean_romanization
      http://en.wikipedia.org/wiki/Revised_Romanization_of_Korean

  The Unicode assignments for Hangul are so regular that it is unnecessary to
  store 11,172 separate caption strings for 0xAC00 to 0xD7A3.  This becomes
  especially true when you realize that only 2,350 of those Hangul syllables
  are in common use! */

  static final String[] HANGUL_NAME_INITIAL = {"Kiyeok", "Ssangkiyeok",
    "Nieun", "Tikeut", "Ssangtikeut", "Rieul", "Mieum", "Pieup", "Ssangpieup",
    "Sios", "Ssangsios", "Ieung", "Cieuc", "Ssangcieuc", "Chieuch", "Khieukh",
    "Thieuth", "Phieuph", "Hieuh"};
  static final String[] HANGUL_NAME_MEDIAL = {"A", "Ae", "Ya", "Yae", "Eo",
    "E", "Yeo", "Ye", "O", "Wa", "Wae", "Oe", "Yo", "U", "Weo", "We", "Wi",
    "Yu", "Eu", "Yi", "I"};
  static final String[] HANGUL_NAME_FINAL = {"", "Kiyeok", "Ssangkiyeok",
    "Kiyeok-Sios", "Nieun", "Nieun-Cieuc", "Nieun-Hieuh", "Tikeut", "Rieul",
    "Rieul-Kiyeok", "Rieul-Mieum", "Rieul-Pieup", "Rieul-Sios",
    "Rieul-Thieuth", "Rieul-Phieuph", "Rieul-Hieuh", "Mieum", "Pieup",
    "Pieup-Sios", "Sios", "Ssangsios", "Ieung", "Cieuc", "Chieuch", "Khieukh",
    "Thieuth", "Phieuph", "Hieuh"};
  static final String[] HANGUL_SOUND_INITIAL = {"G", "KK", "N", "D", "TT", "R",
    "M", "B", "PP", "S", "SS", "", "J", "JJ", "CH", "K", "T", "P", "H"};
  static final String[] HANGUL_SOUND_MEDIAL = {"A", "AE", "YA", "YAE", "EO",
    "E", "YEO", "YE", "O", "WA", "WAE", "OE", "YO", "U", "WO", "WE", "WI",
    "YU", "EU", "UI", "I"};
  static final String[] HANGUL_SOUND_FINAL = {"", "K", "KK", "KS", "N", "NJ",
    "NH", "T", "L", "LK", "LM", "LP", "LS", "LT", "LP", "LH", "M", "P", "PS",
    "S", "SS", "NG", "J", "CH", "K", "T", "P", "H"};

  /* class variables */

  static TreeMap captionMap;      // mapping of character numbers to captions
  static Pattern codePattern;     // compiled expression for Unicode notation
  static Pattern linePattern;     // compiled expression for CJK input line
  static Pattern wordPattern;     // compiled expression for alphanumeric words

  /* main program */

  public static void main(String[] args)
  {
    try                           // catch specific and general I/O errors
    {
      /* Start with an empty mapping from Unicode character numbers (as Integer
      objects) to caption text (as String objects). */

      captionMap = new TreeMap(); // start without any caption strings

      /* Build up the list of known characters, allowing later characters to
      replace earlier characters. */

      uniBegin();                 // parse standard character names
      hanBegin();                 // generate Korean Hangul syllables
      cjkBegin();                 // add or replace CJK ideographs

      /* Extract all known characters and write an output line for each.  A
      second parameter should be added to OutputStreamWriter below if you want
      any character set other than the system default.  (Input data files are
      assumed to be UTF-8 encoded.) */

      PrintWriter output = new PrintWriter(new BufferedWriter(new
        OutputStreamWriter(new FileOutputStream("parsed-unidata.txt"),
        "UTF-8")));
      Set keyList = captionMap.keySet(); // sorted list of character numbers
      Iterator keyIter = keyList.iterator(); // get iterator for those keys
      while (keyIter.hasNext())   // any more character numbers?
      {
        Integer charObj = (Integer) keyIter.next(); // next character as object
        int charNum = charObj.intValue(); // next character as number
        String charText = (String) captionMap.get(charObj); // caption text
        output.println(unicodeNotation(charNum) + " = " + charText);
      }
      output.close();             // try to close output file
    }
    catch (IOException ioe)       // all other I/O errors
    {
      System.err.println("File I/O error: " + ioe.getMessage());
    }
  } // end of main() method

// ------------------------------------------------------------------------- //

/*
  checkPlainText() method

  Given a field value that mostly matches our syntax, look at characters in the
  string and see if they are acceptable to us.  The kMandarin and kVietnamese
  fields have numerous accents, which must be reduced to create US-ASCII plain
  text.  Yes, removing accents does change the meaning of these words!
*/
  static String checkPlainText(String line, String value)
  {
    StringBuffer buffer;          // faster than String for multiple appends
    char ch;                      // one character from input string
    int i;                        // index variable
    int length;                   // size of input string in characters

    buffer = new StringBuffer();  // allocate empty string buffer for result
    length = value.length();      // get size of input string in characters
    for (i = 0; i < length; i ++)
    {
      /* Add new characters as required to the code below.  The official syntax
      for field values is overly general, and would include far more characters
      than are actually needed. */

      ch = value.charAt(i);       // get one character from input string
      if ((ch >= 0x20) && (ch <= 0x7E)) // printable US-ASCII plain text
        buffer.append(ch);        // accept this character as-is
      else if (ch == 0x00E0)      // Latin Small Letter A With Grave (M, V)
        buffer.append('a');
      else if (ch == 0x00E1)      // Latin Small Letter A With Acute (M, V)
        buffer.append('a');
      else if (ch == 0x00E2)      // Latin Small Letter A With Circumflex (V)
        buffer.append('a');
      else if (ch == 0x00E3)      // Latin Small Letter A With Tilde (V)
        buffer.append('a');
      else if (ch == 0x00E8)      // Latin Small Letter E With Grave (M, V)
        buffer.append('e');
      else if (ch == 0x00E9)      // Latin Small Letter E With Acute (M, V)
        buffer.append('e');
      else if (ch == 0x00EA)      // Latin Small Letter E With Circumflex (V)
        buffer.append('e');
      else if (ch == 0x00EC)      // Latin Small Letter I With Grave (M, V)
        buffer.append('i');
      else if (ch == 0x00ED)      // Latin Small Letter I With Acute (M, V)
        buffer.append('i');
      else if (ch == 0x00F2)      // Latin Small Letter O With Grave (M, V)
        buffer.append('o');
      else if (ch == 0x00F3)      // Latin Small Letter O With Acute (M, V)
        buffer.append('o');
      else if (ch == 0x00F4)      // Latin Small Letter O With Circumflex (V)
        buffer.append('o');
      else if (ch == 0x00F5)      // Latin Small Letter O With Tilde (V)
        buffer.append('o');
      else if (ch == 0x00F9)      // Latin Small Letter U With Grave (M, V)
        buffer.append('u');
      else if (ch == 0x00FA)      // Latin Small Letter U With Acute (M, V)
        buffer.append('u');
      else if (ch == 0x00FC)      // Latin Small Letter U With Diaeresis (M)
        buffer.append('u');
      else if (ch == 0x00FD)      // Latin Small Letter Y With Acute (V)
        buffer.append('y');
      else if (ch == 0x0101)      // Latin Small Letter A With Macron (M)
        buffer.append('a');
      else if (ch == 0x0103)      // Latin Small Letter A With Breve (V)
        buffer.append('a');
      else if (ch == 0x0111)      // Latin Small Letter D With Stroke (V)
        buffer.append('d');
      else if (ch == 0x0113)      // Latin Small Letter E With Macron (M)
        buffer.append('e');
      else if (ch == 0x011B)      // Latin Small Letter E With Caron (M)
        buffer.append('e');
      else if (ch == 0x0129)      // Latin Small Letter I With Tilde (V)
        buffer.append('i');
      else if (ch == 0x012B)      // Latin Small Letter I With Macron (M)
        buffer.append('i');
      else if (ch == 0x0144)      // Latin Small Letter N With Acute (M?)
        buffer.append('n');
      else if (ch == 0x0148)      // Latin Small Letter N With Caron (M?)
        buffer.append('n');
      else if (ch == 0x014D)      // Latin Small Letter O With Macron (M)
        buffer.append('o');
      else if (ch == 0x0169)      // Latin Small Letter U With Tilde (V)
        buffer.append('u');
      else if (ch == 0x016B)      // Latin Small Letter U With Macron (M)
        buffer.append('u');
      else if (ch == 0x01A1)      // Latin Small Letter O With Horn (V)
        buffer.append('o');
      else if (ch == 0x01B0)      // Latin Small Letter U With Horn (V)
        buffer.append('u');
      else if (ch == 0x01CE)      // Latin Small Letter A With Caron (M)
        buffer.append('a');
      else if (ch == 0x01D0)      // Latin Small Letter I With Caron (M)
        buffer.append('i');
      else if (ch == 0x01D2)      // Latin Small Letter O With Caron (M)
        buffer.append('o');
      else if (ch == 0x01D4)      // Latin Small Letter U With Caron (M)
        buffer.append('u');
      else if (ch == 0x01D8)      // Latin Small Letter U With Diaeresis And Acute (M)
        buffer.append('u');
      else if (ch == 0x01DA)      // Latin Small Letter U With Diaeresis And Caron (M)
        buffer.append('u');
      else if (ch == 0x01DC)      // Latin Small Letter U With Diaeresis And Grave (M)
        buffer.append('u');
      else if (ch == 0x01F9)      // Latin Small Letter N With Grave (M?)
        buffer.append('n');
      else if (ch == 0x1E3F)      // Latin Small Letter M With Acute (M?)
        buffer.append('m');
      else if (ch == 0x1EA1)      // Latin Small Letter A With Dot Below (V)
        buffer.append('a');
      else if (ch == 0x1EA3)      // Latin Small Letter A With Hook Above (V)
        buffer.append('a');
      else if (ch == 0x1EA5)      // Latin Small Letter A With Circumflex And Acute (V)
        buffer.append('a');
      else if (ch == 0x1EA7)      // Latin Small Letter A With Circumflex And Grave (V)
        buffer.append('a');
      else if (ch == 0x1EA9)      // Latin Small Letter A With Circumflex And Hook Above (V)
        buffer.append('a');
      else if (ch == 0x1EAB)      // Latin Small Letter A With Circumflex And Tilde (V)
        buffer.append('a');
      else if (ch == 0x1EAD)      // Latin Small Letter A With Circumflex And Dot Below (V)
        buffer.append('a');
      else if (ch == 0x1EAF)      // Latin Small Letter A With Breve And Acute (V)
        buffer.append('a');
      else if (ch == 0x1EB1)      // Latin Small Letter A With Breve And Grave (V)
        buffer.append('a');
      else if (ch == 0x1EB3)      // Latin Small Letter A With Breve And Hook Above (V)
        buffer.append('a');
      else if (ch == 0x1EB5)      // Latin Small Letter A With Breve And Tilde (V)
        buffer.append('a');
      else if (ch == 0x1EB7)      // Latin Small Letter A With Breve And Dot Below (V)
        buffer.append('a');
      else if (ch == 0x1EB9)      // Latin Small Letter E With Dot Below (V)
        buffer.append('e');
      else if (ch == 0x1EBB)      // Latin Small Letter E With Hook Above (V)
        buffer.append('e');
      else if (ch == 0x1EBD)      // Latin Small Letter E With Tilde (V)
        buffer.append('e');
      else if (ch == 0x1EBF)      // Latin Small Letter E With Circumflex And Acute (V)
        buffer.append('e');
      else if (ch == 0x1EC1)      // Latin Small Letter E With Circumflex And Grave (V)
        buffer.append('e');
      else if (ch == 0x1EC3)      // Latin Small Letter E With Circumflex And Hook Above (V)
        buffer.append('e');
      else if (ch == 0x1EC5)      // Latin Small Letter E With Circumflex And Tilde (V)
        buffer.append('e');
      else if (ch == 0x1EC7)      // Latin Small Letter E With Circumflex And Dot Below (V)
        buffer.append('e');
      else if (ch == 0x1EC9)      // Latin Small Letter I With Hook Above (V)
        buffer.append('i');
      else if (ch == 0x1ECB)      // Latin Small Letter I With Dot Below (V)
        buffer.append('i');
      else if (ch == 0x1ECD)      // Latin Small Letter O With Dot Below (V)
        buffer.append('o');
      else if (ch == 0x1ECF)      // Latin Small Letter O With Hook Above (V)
        buffer.append('o');
      else if (ch == 0x1ED1)      // Latin Small Letter O With Circumflex And Acute (V)
        buffer.append('o');
      else if (ch == 0x1ED3)      // Latin Small Letter O With Circumflex And Grave (V)
        buffer.append('o');
      else if (ch == 0x1ED5)      // Latin Small Letter O With Circumflex And Hook Above (V)
        buffer.append('o');
      else if (ch == 0x1ED7)      // Latin Small Letter O With Circumflex And Tilde (V)
        buffer.append('o');
      else if (ch == 0x1ED9)      // Latin Small Letter O With Circumflex And Dot Below (V)
        buffer.append('o');
      else if (ch == 0x1EDB)      // Latin Small Letter O With Horn And Acute (V)
        buffer.append('o');
      else if (ch == 0x1EDD)      // Latin Small Letter O With Horn And Grave (V)
        buffer.append('o');
      else if (ch == 0x1EDF)      // Latin Small Letter O With Horn And Hook Above (V)
        buffer.append('o');
      else if (ch == 0x1EE1)      // Latin Small Letter O With Horn And Tilde (V)
        buffer.append('o');
      else if (ch == 0x1EE3)      // Latin Small Letter O With Horn And Dot Below (V)
        buffer.append('o');
      else if (ch == 0x1EE5)      // Latin Small Letter U With Dot Below (V)
        buffer.append('u');
      else if (ch == 0x1EE7)      // Latin Small Letter U With Hook Above (V)
        buffer.append('u');
      else if (ch == 0x1EE9)      // Latin Small Letter U With Horn And Acute (V)
        buffer.append('u');
      else if (ch == 0x1EEB)      // Latin Small Letter U With Horn And Grave (V)
        buffer.append('u');
      else if (ch == 0x1EED)      // Latin Small Letter U With Horn And Hook Above (V)
        buffer.append('u');
      else if (ch == 0x1EEF)      // Latin Small Letter U With Horn And Tilde (V)
        buffer.append('u');
      else if (ch == 0x1EF1)      // Latin Small Letter U With Horn And Dot Below (V)
        buffer.append('u');
      else if (ch == 0x1EF3)      // Latin Small Letter Y With Grave (V)
        buffer.append('y');
      else if (ch == 0x1EF5)      // Latin Small Letter Y With Dot Below (V)
        buffer.append('y');
      else if (ch == 0x1EF7)      // Latin Small Letter Y With Hook Above (V)
        buffer.append('y');
      else if (ch == 0x1EF9)      // Latin Small Letter Y With Tilde (V)
        buffer.append('y');
      else                        // unknown character, this program needs help
      {
        buffer.append('?');       // replace anything we don't recognize
        System.err.println("Unknown character " + unicodeNotation(ch)
          + " input <" + line + ">");
      }
    }
    return(buffer.toString());    // give caller our converted string

  } // end of checkPlainText() method


/*
  cjkBegin() method

  Extract pronunciation and variant notations for Chinese-Japanese-Korean (CJK)
  ideographs from the Unihan data files.
*/
  static void cjkBegin() throws IOException
  {
    StringBuffer caption;         // for building the CJK caption text
    TreeMap cjkListMap;           // map character numbers to CJK word lists
    int code;                     // Unicode character number
    String field;                 // field name
    int i;                        // index variable
    BufferedReader input;         // input character stream
    Integer key;                  // index key into map of CJK word lists
    String line;                  // one line of text from input file
    Matcher matcher;              // pattern matcher for regular expressions
    String prefix;                // first part of saved word: language tag
    int size;                     // number of words in this CJK word list
    String suffix;                // second part of saved word: information
    String tag;                   // language tag found during sorting
    String value;                 // field value
    Vector wordList;              // one CJK word list

    cjkListMap = new TreeMap();   // start without any CJK word lists

    /* Compile regular expressions once only. */

    codePattern = Pattern.compile("U\\+[0-9A-Fa-f]{1,6}");
    linePattern = Pattern.compile("^\\s*U\\+([0-9A-Fa-f]{1,6})\\s+([^\\s]+)\\s+(.+)$");
    wordPattern = Pattern.compile("[A-Za-z]+"); // ignore digit suffix for tone

    /* The first input file has the following fields of interest: kCantonese,
    kJapaneseKun, kJapaneseOn, kKorean, kMandarin, kVietnamese. */

    input = new BufferedReader(new InputStreamReader(new FileInputStream(
      "Unihan_Readings.txt"), "UTF-8"));
    while ((line = input.readLine()) != null)
    {
      matcher = linePattern.matcher(line); // attempt to match input line
      if (matcher.find())         // if the general search pattern is found
      {
        code = Integer.parseInt(matcher.group(1), 16); // character number
        field = matcher.group(2); // field name
        value = matcher.group(3);
        if (field.equals("kCantonese"))
        {
          value = checkPlainText(line, value); // check for plain text
          cjkNewWords(cjkListMap, code, value, "C:");
        }
        else if (field.equals("kJapaneseKun"))
        {
          value = checkPlainText(line, value); // check for plain text
          cjkNewWords(cjkListMap, code, value, "J:");
        }
        else if (field.equals("kJapaneseOn"))
        {
          value = checkPlainText(line, value); // check for plain text
          cjkNewWords(cjkListMap, code, value, "S:");
        }
        else if (field.equals("kKorean"))
        {
          value = checkPlainText(line, value); // check for plain text
          cjkNewWords(cjkListMap, code, value, "K:");
        }
        else if (field.equals("kMandarin")) // some accented pinyin
        {
          value = checkPlainText(line, value); // check for plain text
          cjkNewWords(cjkListMap, code, value, "M:");
        }
        else if (field.equals("kVietnamese")) // many accented vowels
        {
          value = checkPlainText(line, value); // check for plain text
          cjkNewWords(cjkListMap, code, value, "V:");
        }
      }
    }
    input.close();                // try to close input file

    /* The second input file has variants (similar meanings).  We accept all
    Unicode character numbers without asking where they come from. */

    input = new BufferedReader(new InputStreamReader(new FileInputStream(
      "Unihan_Variants.txt"), "UTF-8"));
    while ((line = input.readLine()) != null)
    {
      matcher = linePattern.matcher(line); // attempt to match input line
      if (matcher.find())         // if the general search pattern is found
      {
        code = Integer.parseInt(matcher.group(1), 16); // character number
//      field = matcher.group(2); // ignore field name
        value = matcher.group(3);
        value = checkPlainText(line, value); // check for plain text
        cjkNewCodes(cjkListMap, code, value); // accept all variants
      }
    }
    input.close();                // try to close input file

    /* Sort the collected CJK word lists and add them to the global caption
    map.  We recreate Unicode notation for variants, to remove leading zeros
    that were added to get the correct sorting order. */

    Set keyList = cjkListMap.keySet(); // sorted list of character numbers
    Iterator keyIter = keyList.iterator(); // get iterator for those keys
    while (keyIter.hasNext())     // any more character numbers?
    {
      key = (Integer) keyIter.next(); // next character number as object
      wordList = (Vector) cjkListMap.get(key); // shouldn't be null
      Collections.sort(wordList); // sort list of words as Unicode strings
      size = wordList.size();     // number of words in this CJK word list

      caption = new StringBuffer(); // for building the CJK caption text
      tag = EMPTY;                // no language tag found yet
      for (i = 0; i < size; i ++) // do each word in word list
      {
        if (i > 0)                // insert space for second and later word
          caption.append(' ');    // put the delimiter before the word

        value = (String) wordList.get(i); // get one interesting word
        prefix = value.substring(0, 2); // our language tag
        suffix = value.substring(2); // actual information from data file
        if (prefix.equals(VARIANT_TAG)) // don't show fake tag for variants
          caption.append(unicodeNotation(Integer.parseInt(suffix, 16)));
        else if (prefix.equals(tag))
          caption.append(suffix); // don't repeat language tag
        else
          caption.append(value);  // first time we've seen this language tag

        tag = prefix;             // don't repeat this language tag next time
      }
      putCaption(key.intValue(), caption.toString());
                                  // save caption for this character
    }
  } // end of cjkBegin() method


/*
  cjkNewCodes() method

  Given a string, add each Unicode notation in that string to our list of
  interesting words.  Pad with leading zeros so they sort in numerical order
  instead of string order, i.e., U+53A8 before U+2228D.
*/
  static void cjkNewCodes(TreeMap listmap, int code, String text)
  {
    Matcher matcher;              // pattern matcher for regular expressions
    int value;                    // parsed Unicode character number
    String word;                  // one word found in caller's text

    matcher = codePattern.matcher(text); // start looking for words
    while (matcher.find())        // for all words that we find
    {
      word = matcher.group().toUpperCase(); // get "U+nnnn" as text
      word = "00000000".substring(word.length() - 2) + word.substring(2);
                                  // drop "U+" notation, zero pad hex number
      value = Integer.parseInt(word, 16); // parse numeric part of "U+nnnn"
      if (code != value)          // CJK character can't be variant of itself
        cjkNewEntry(listmap, code, (VARIANT_TAG + word)); // insert into list
      else                        // variant self reference is useless for us
      {
//      System.err.println("CJK variant references itself: "
//        + unicodeNotation(code));
      }
    }
  } // end of cjkNewCodes() method


/*
  cjkNewEntry() method

  Common method called by cjkNewCodes() and cjkNewWords() to insert one word
  into our list of interesting words.
*/
  static void cjkNewEntry(TreeMap listmap, int code, String text)
  {
    Integer key;                  // index key into map of CJK word lists
    Vector wordList;              // one list of words already collected

    key = new Integer(code);      // where to look in the TreeMap
    wordList = (Vector) listmap.get(key); // get current list of words
    if (wordList == null)         // if there is no word list yet
    {
      wordList = new Vector();    // then create new word list
      listmap.put(key, wordList); // link new list into map of word lists
    }
    if (wordList.contains(text) == false) // is this word already in list?
      wordList.add(text);         // no, add new word to list
  }


/*
  cjkNewWords() method

  Given a string, add each alphanumeric word in that string to our list of
  interesting words.
*/
  static void cjkNewWords(TreeMap listmap, int code, String text, String tag)
  {
    Matcher matcher;              // pattern matcher for regular expressions
    String word;                  // one word found in caller's text

    matcher = wordPattern.matcher(text); // start looking for words
    while (matcher.find())        // for all words that we find
    {
      word = matcher.group();     // need to format word in title case
      cjkNewEntry(listmap, code, (tag + word.substring(0, 1).toUpperCase()
        + word.substring(1).toLowerCase())); // insert found word in list
    }
  }


/*
  hanBegin() method

  Generate Korean Hangul syllables and sounds similar to the data file:

  http://www.iana.org/domains/idn-tables/tables/kr_ko-kr_1.0.html
  linked from: http://www.iana.org/domains/idn-tables/
  was (c.2008): http://www.iana.org/assignments/idn/kr-korean.html
*/
  static void hanBegin()
  {
    String caption;               // generated caption string
    int value;                    // Unicode character number

    /* The following code was obtained from the CharMap4 Java application, with
    minimal changes (to verify that the code works as expected). */

    for (value = 0xAC00; value <= 0xD7A3; value ++) // Korean Hangul range
    {
      int first = value - 0xAC00; // set zero point for following calculation
      int third = first % 28;     // index of "final" phonetic piece
      first = first / 28;         // remove value of final piece
      int second = first % 21;    // index of "medial" phonetic piece
      first = first / 21;         // index of "initial" phonetic piece

      caption = "Hangul Syllable " + HANGUL_NAME_INITIAL[first] + " "
        + HANGUL_NAME_MEDIAL[second] + " " + HANGUL_NAME_FINAL[third];
      caption = caption.trim();   // remove any unused third piece
      String sound = HANGUL_SOUND_INITIAL[first]
        + HANGUL_SOUND_MEDIAL[second] + HANGUL_SOUND_FINAL[third];
      caption += " (" + sound.charAt(0) + sound.substring(1).toLowerCase()
        + ")";                    // first "letter" may be from second piece

      putCaption(value, caption); // save caption for this character
    }
  } // end of hanBegin() method


/*
  putCaption() method

  Save caption text (string) corresponding to a character value.  Use a common
  method for this simple operation, so that everyone does it the same way.
*/
  static void putCaption(int value, String text)
  {
    captionMap.put(new Integer(value), text);
  }


/*
  titleCase() method

  Return a string with everything in lowercase except the first letter of each
  word.  Unicode standard names are all uppercase by definition; however, don't
  assume that the caller's string is uppercase.
*/
  static String titleCase(String input)
  {
    StringBuffer buffer;          // faster than String for multiple appends
    char ch;                      // one input character
    int i;                        // index variable
    int length;                   // number of input characters
    boolean upper;                // true if next character will be uppercase

    buffer = new StringBuffer();  // allocate empty string buffer for result
    length = input.length();      // get number of input characters
    upper = true;                 // first word is always capitalized
    for (i = 0; i < length; i ++) // do all input characters
    {
      ch = input.charAt(i);       // get one input character
      if ((ch == ' ') || (ch == '-')) // check for word delimiters
      {
        buffer.append(ch);        // copy delimiter unchanged
        upper = true;             // next letter will be uppercase
      }
      else if (upper)             // should we leave this as uppercase?
      {
        buffer.append(Character.toUpperCase(ch)); // append as uppercase
        upper = false;            // next character will be lowercase
      }
      else
        buffer.append(Character.toLowerCase(ch)); // append as lowercase
    }
    return(buffer.toString());    // give caller our converted string

  } // end of titleCase() method


/*
  uniBegin() method

  Extract standard Unicode character names from the UnicodeData.txt data file.
*/
  static void uniBegin() throws IOException
  {
    BufferedReader input;         // input character stream
    String line;                  // one line of text from input file
    Matcher matcher;              // pattern matcher for <pattern>
    Pattern pattern;              // compiled regular expression

    input = new BufferedReader(new InputStreamReader(new FileInputStream(
      "UnicodeData.txt"), "UTF-8"));
    pattern = Pattern.compile(    // regular expression for lines we want
      "^\\s*([0-9A-Fa-f]{1,6})\\s*;\\s*(\\S[^;]*\\S)\\s*;");
    while ((line = input.readLine()) != null)
    {
      matcher = pattern.matcher(line); // attempt to match
      if (matcher.find())         // if the search pattern is found
      {
        putCaption(Integer.parseInt(matcher.group(1), 16), // hex char number
          titleCase(matcher.group(2))); // caption text for character
      }
    }
    input.close();                // try to close input file

  } // end of uniBegin() method


/*
  unicodeNotation() method

  Given an integer, return the Unicode "U+nnnn" notation for that character
  number.
*/
  static String unicodeNotation(int value)
  {
    String result;                // our converted result

    result = Integer.toHexString(value).toUpperCase(); // convert binary to hex
    if (result.length() < 4)      // must have at least four digits
      result = "0000".substring(result.length()) + result;
    result = "U+" + result;       // insert the "U+" prefix

    return(result);               // give caller our converted string
  }

} // end of CharMapParse6 class

/* Copyright (c) 2016 by Keith Fenske.  Apache License or GNU GPL. */
