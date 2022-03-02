/*
  Character Map Parse #1 - Quick-and-Dirty Extraction of UNIDATA/Blocks.txt
  Written by: Keith Fenske, http://kwfenske.github.io/
  Monday, 12 January 2009
  Java class name: CharMapParse1
  Copyright (c) 2009 by Keith Fenske.  Apache License or GNU GPL.

  This is a quick-and-dirty Java 1.4 console application to parse the data file
  downloaded from

      http://www.unicode.org/Public/UNIDATA/Blocks.txt

  to obtain Unicode "block" or range names.  Output is formatted as source code
  to be inserted into the captionGet() method of the CharMap4 Java application.
  Some manual editing will be required on the first line ("if" instead of "else
  if").  This program assumes that block names do not contain backslashes (\),
  control codes, escape sequences, quotation marks ("), or any other characters
  that would cause syntax errors in the generated source code.

  CharMapParse1 should be run once each time the Unicode standard is revised.
  There are no parameters.  The "Blocks.txt" data file must be in the current
  working directory.  Output goes into a file called "parsed-blocks.txt" using
  the system's default character set for easy copy-and-paste.

  Written for Unicode 5.1.0 (2008) and still working on Unicode 11.0.0 (2018).
  Strictly speaking, "Blocks.txt" changed from US-ASCII plain text to UTF-8 in
  the copyright notice, as of version 9.0.0 (2016).  This source file should
  only be distributed with the source for CharMap4.  General users have no need
  for the CharMapParse1 application.  THIS CODE IS UGLY AND SHOULD *NOT* BE
  USED AS THE BASIS FOR ANY OTHER PROGRAMS.

  Apache License or GNU General Public License
  --------------------------------------------
  CharMapParse1 is free software and has been released under the terms and
  conditions of the Apache License (version 2.0 or later) and/or the GNU
  General Public License (GPL, version 2 or later).  This program is
  distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY,
  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE.  See the license(s) for more details.  You should have
  received a copy of the licenses along with this program.  If not, see the
  http://www.apache.org/licenses/ and http://www.gnu.org/licenses/ web pages.
*/

import java.io.*;                 // standard I/O
import java.util.regex.*;         // regular expressions

public class CharMapParse1
{
  public static void main(String[] args)
  {
    BufferedReader input;         // input character stream
    String line;                  // one line of text from input file
    Matcher matcher;              // pattern matcher for <pattern>
    PrintWriter output;           // output character stream
    Pattern pattern;              // compiled regular expression

    try                           // catch specific and general I/O errors
    {
      input = new BufferedReader(new InputStreamReader(new FileInputStream(
        "Blocks.txt"), "UTF-8")); // mostly US-ASCII with some UTF-8 comments
      output = new PrintWriter(new BufferedWriter(new FileWriter(
        "parsed-blocks.txt")));
      pattern = Pattern.compile(
        "^\\s*([0-9A-Fa-f]+)\\s*\\.\\.\\s*([0-9A-Fa-f]+)\\s*;\\s*(\\S.*\\S)\\s*$");
      while ((line = input.readLine()) != null)
      {
        matcher = pattern.matcher(line); // attempt to match
        if (matcher.find())       // if the search pattern is found
        {
          output.println("      else if ((value >= 0x"
            + matcher.group(1).toUpperCase() + ") && (value <= 0x"
            + matcher.group(2).toUpperCase() + "))");
          output.println("        caption = \"" + matcher.group(3) + "\";");
        }
      }
      input.close();              // try to close input file
      output.close();             // try to close output file
    }
    catch (IOException ioe)       // all other I/O errors
    {
      System.err.println("File I/O error: " + ioe.getMessage());
    }
  } // end of main() method

} // end of CharMapParse1 class

/* Copyright (c) 2009 by Keith Fenske.  Apache License or GNU GPL. */
