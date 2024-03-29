/*
  Character Map #4 - Display Characters and Copy to System Clipboard
  Written by: Keith Fenske, http://kwfenske.github.io/
  Monday, 19 May 2008
  Java class name: CharMap4
  Copyright (c) 2008 by Keith Fenske.  Apache License or GNU GPL.

  This is a Java 5.0 graphical (GUI) application to display Unicode characters
  or glyphs in text fonts, and copy those characters to the system clipboard.
  Its major purpose is as a visual accessory for word processors such as
  Microsoft Word.  The "character map" utility that comes with Windows suffers
  from several problems.  This Java application can be resized, for text and
  the program window, which is important in many languages.  Features are
  limited to make the application faster and simpler to use.  A single click
  adds a character to the sample text, and the sample text is automatically
  copied to the system clipboard on each click.

  You may choose the font to be displayed and the size of the characters or
  glyphs.  (Glyphs are bits and pieces that a font combines to produce the
  characters you see.  In most cases, one character maps to one glyph.)  You
  may edit the sample text, erase it with the "Clear" button, or copy it to the
  system clipboard with the "Copy All" button.  Paste the text into your word
  processor in the normal manner, which is usually a Control-V key combination.
  Editing the sample text and pressing the Enter key also copies to the
  clipboard.  Specific characters can be copied from the sample text by
  selection and with the usual Control-C combination.  More characters are
  available via the scroll bar on the right.  A description is shown in the
  "caption" field when characters have a particular name or meaning.  Common
  readings or sounds are given for Chinese, Japanese, and Korean characters.
  Cantonese is prefixed with "C", Japanese "Kun" with "J", Korean with "K",
  Mandarin with "M", and Sino-Japanese "On" with "S".  An English translation
  of CJK character definitions would have been more amusing but less practical.

  Keyboard shortcuts are provided to mimic the scroll bar: the Control-Home key
  combination goes to the very first character, Control-End goes to the last
  character, Page Down and Page Up scroll one screen at a time, and the arrow
  keys scroll one line at a time.  You need to combine the End and Home keys
  with the Control (Ctrl) key when the sample text has keyboard focus.  The F1
  key is the only helpful undocumented feature.

  Apache License or GNU General Public License
  --------------------------------------------
  CharMap4 is free software and has been released under the terms and
  conditions of the Apache License (version 2.0 or later) and/or the GNU
  General Public License (GPL, version 2 or later).  This program is
  distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY,
  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE.  See the license(s) for more details.  You should have
  received a copy of the licenses along with this program.  If not, see the
  http://www.apache.org/licenses/ and http://www.gnu.org/licenses/ web pages.

  Graphical Versus Console Application
  ------------------------------------
  The Java command line may contain options for the initial display font, the
  size of the text, and the window position.  See the "-?" option for a help
  summary:

      java  CharMap4  -?

  The command line has more options than are visible in the graphical
  interface.  An option such as -u14 or -u16 is recommended because the default
  Java font is too small.

  Restrictions and Limitations
  ----------------------------
  Which fonts will work with this program depends upon the operating system and
  version of the Java run-time environment.  Java 5.0 and 6 on Windows
  2000/XP/Vista/7 show installed TrueType fonts, that is, fonts added with the
  Control Panel, Fonts icon.  (Temporary fonts are not shown if opened with the
  Windows Font Viewer by double clicking on a font file name.)  If you think
  this program is not working correctly on your computer, then "Lucida Console"
  is a good font for testing the spacing and positioning, because its glyphs
  are tightly packed.  Version 4 of CharMap supports extended Unicode (up to
  1,114,112 characters) and is noticeably slower than version 3, which only
  supports the standard range of 65,536 characters.  Version 4 also tends to
  run out of memory for very large fonts; see the -Xmx option on the Java
  command line.

  This program contains character data from the Unicode Consortium; please
  visit their web site at http://www.unicode.org/ for more information.  Korean
  character names were converted from Korean standards document KS X 1001:2002
  with the title "Hangeul Syllables in Unicode 4.0" and dated 25 March 2004.  A
  plain text file called "CharMap4.txt" is expected to be in the current
  working directory with mappings from Unicode character numbers to caption
  strings.  You may edit this file to produce whatever captions you wish.
  Please read comments in the file for further instructions.

  Suggestions for New Features
  ----------------------------
  (1) Add "print" feature with titles, page numbers, and annotations for each
      grid cell (glyph number, Unicode character notation).  KF, 2008-02-10.
  (2) A grid is a good way to select special characters in small fonts.  For
      very large fonts (Chinese, Japanese, Korean, Unicode), add an optional
      filter that shows only characters whose description (or Unicode block
      range) contains a caseless string given by the user.  KF, 2010-04-20.
  (3) Some fonts for mathematical equations ("Cambria Math") draw outside the
      declared maximum height and width.  Glyphs may be clipped (cropped) on
      the right or below because this program uses a single pass for the grid:
      draw one square and its glyph, then the next square.  Two passes may be
      required: first draw all squares, then draw all glyphs.  KF, 2011-07-31.
*/

import java.awt.*;                // older Java GUI support
import java.awt.event.*;          // older Java GUI event support
import java.awt.font.*;           // low-level glyphs instead of characters
import java.io.*;                 // standard I/O
import java.text.*;               // number formatting
import java.util.*;               // calendars, dates, lists, maps, vectors
import java.util.regex.*;         // regular expressions
import javax.swing.*;             // newer Java GUI support
import javax.swing.event.*;       // change listener

public class CharMap4
{
  /* constants */

  static final String ACTION_GOTO_END = "GotoEnd"; // keyboard action strings
  static final String ACTION_GOTO_HOME = "GotoHome";
  static final String ACTION_LINE_DOWN = "LineDown";
  static final String ACTION_LINE_UP = "LineUp";
  static final String ACTION_PAGE_DOWN = "PageDown";
  static final String ACTION_PAGE_UP = "PageUp";
  static final String ACTION_PROGRAM_EXIT = "ProgramExit";
  static final String ACTION_REPORT_HIDE = "ReportHide";
  static final String ACTION_REPORT_SHOW = "ReportShow";
  static final String ACTION_SAMPLE_CLEAR = "SampleClear";
  static final String COPYRIGHT_NOTICE =
    "Copyright (c) 2008 by Keith Fenske.  Apache License or GNU GPL.";
  static final String DEFAULT_FILE = "CharMap4.txt"; // data file with names
  static final String DEFAULT_FONT = "Verdana"; // default font name
  static final int DEFAULT_HEIGHT = 500; // default window height in pixels
  static final int DEFAULT_LEFT = 50; // default window left position ("x")
  static final int DEFAULT_SIZE = 30; // default point size for display text
  static final int DEFAULT_TOP = 50; // default window top position ("y")
  static final int DEFAULT_WIDTH = 700; // default window width in pixels
  static final String EMPTY_STATUS = " "; // message when no status to display
  static final String[] FONT_SIZES = {"18", "24", "30", "36", "48", "60", "72",
    "96"};                        // standard point sizes for display text
  static final String LICENSE_FILE = "GnuPublicLicense3.txt";
  static final String LICENSE_NAME = "GNU General Public License (GPL)";
  static final int MAX_SIZE = 999; // maximum point size for display text
  static final int MIN_FRAME = 200; // minimum window height or width in pixels
  static final int MIN_SIZE = 10; // minimum point size for display text
  static final String PROGRAM_TITLE =
    "Display Characters and Copy to System Clipboard - by: Keith Fenske";
  static final char REPLACE_CHAR = '\uFFFD'; // Unicode replacement character
  static final String SYSTEM_FONT = "Dialog"; // this font is always available

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

  /* The following constants are our definitions for the supported Unicode
  range.  We use names separate from the Java standard, so that we can more
  easily compile 16-bit Unicode on Java 1.4 and extended Unicode on Java 5.0
  or later.  Only a few lines in this entire program depend upon the Unicode
  range, and all have "Java 1.4" or "Java 5.0" comment flags.  If you want a
  character map to start from something other than binary zero (such as from
  0x20 or U+0020 for a space), then change MIN_UNICODE.  Both the minimum and
  the maximum are assumed to be non-negative integers, with the maximum larger
  than the minimum. */

//static final int MAX_UNICODE = Character.MAX_VALUE; // Java 1.4 0xFFFF
//static final int MIN_UNICODE = Character.MIN_VALUE; // Java 1.4 0x0000

  static final int MAX_UNICODE = Character.MAX_CODE_POINT; // Java 5.0 0x10FFFF
  static final int MIN_UNICODE = Character.MIN_CODE_POINT; // Java 5.0 0x000000

  /* class variables */

  static Font buttonFont;         // font for buttons, labels, status, etc
  static TreeMap captionMap;      // mapping of char values to mouse captions
  static JButton clearButton;     // graphical button to clear <sampleDialog>
  static boolean clickReplace;    // true if mouse click replaces sample text
  static JButton copyButton;      // graphical button to copy all text
  static String dataFile;         // text file with configuration data
  static Font displayFont;        // common font object for display text
  static String fontName;         // font name for display text
  static int fontSize;            // point size for display text
  static NumberFormat formatComma; // formats with commas (digit grouping)
  static boolean glyphFlag;       // true if showing glyphs, false for chars
  static CharMap4Grid gridPanel;  // displays a grid of characters for a font
  static JScrollBar gridScroll;   // vertical scroll bar beside <gridPanel>
  static JFrame mainFrame;        // this application's window
  static JButton menuButton;      // generic "Menu" button
  static JMenuItem menuChars, menuExit, menuGlyphs, menuReport;
                                  // menu items for <menuPopup>
  static JPopupMenu menuPopup;    // pop-up menu invoked by <menuButton>
  static boolean mswinFlag;       // true if running on Microsoft Windows
  static JComboBox nameDialog;    // graphical option for <fontName>
  static JButton reportCloseButton, reportLicenseButton, reportSummaryButton;
  static JFrame reportDialog;     // common dialog used by license or summary
  static JTextArea reportText;    // output text for license or summary report
  static JMenuItem rightCopyCaption, rightCopyGlyph, rightCopyNotation,
    rightCopyNumber, rightCopyText; // menu items for <rightPopup>
  static JPopupMenu rightPopup;   // pop-up menu invoked by right mouse click
  static String rightSaveCaption; // saved caption text for right mouse click
  static int rightSaveChar, rightSaveGlyph; // saved character, glyph numbers
  static JTextField sampleDialog; // characters selected, copied to clipboard
  static JComboBox sizeDialog;    // graphical option for <fontSize>
  static JLabel statusDialog;     // for mouse captions to identify characters
  static ActionListener userActions; // our shared action listener
  static int windowHeight, windowLeft, windowTop, windowWidth;
                                  // position and size for <mainFrame>

/*
  main() method

  We run as a graphical application only.  Set the window layout and then let
  the graphical interface run the show.
*/
  public static void main(String[] args)
  {
    int i;                        // index variable
    Thread loader;                // low-priority thread for loading captions
    boolean maximizeFlag;         // true if we maximize our main window
    String sampleText;            // setup string that becomes <sampleDialog>
    String word;                  // one parameter from command line

    /* Initialize global variables that may be affected by options on the
    command line. */

    buttonFont = null;            // by default, don't use customized font
    captionMap = null;            // mouse captions are not yet available
    clickReplace = false;         // default mouse click to insert sample text
    dataFile = DEFAULT_FILE;      // default file name for configuration data
    displayFont = null;           // during setup, there is no display font
    fontName = DEFAULT_FONT;      // default font name for display text
    fontSize = DEFAULT_SIZE;      // default point size for display text
    glyphFlag = false;            // by default, show characters not glyphs
    gridPanel = null;             // during setup, there is no character grid
    mainFrame = null;             // during setup, there is no GUI window
    maximizeFlag = false;         // by default, don't maximize our main window
    mswinFlag = System.getProperty("os.name").startsWith("Windows");
    reportDialog = null;          // by default, report dialog not yet created
    sampleText = "";              // by default, there is no sample text
    windowHeight = DEFAULT_HEIGHT; // default window position and size
    windowLeft = DEFAULT_LEFT;
    windowTop = DEFAULT_TOP;
    windowWidth = DEFAULT_WIDTH;

    /* Initialize number formatting styles. */

    formatComma = NumberFormat.getInstance(); // current locale
    formatComma.setGroupingUsed(true); // use commas or digit groups

    /* Check command-line parameters for options.  Anything we don't recognize
    as an option is assumed to be an initial text sample. */

    for (i = 0; i < args.length; i ++)
    {
      word = args[i].toLowerCase(); // easier to process if consistent case
      if (word.length() == 0)
      {
        /* Ignore empty parameters, which are more common than you might think,
        when programs are being run from inside scripts (command files). */
      }

      else if (word.equals("?") || word.equals("-?") || word.equals("/?")
        || word.equals("-h") || (mswinFlag && word.equals("/h"))
        || word.equals("-help") || (mswinFlag && word.equals("/help")))
      {
        showHelp();               // show help summary
        System.exit(0);           // exit application after printing help
      }

      else if (word.equals("-c") || (mswinFlag && word.equals("/c"))
        || word.equals("-c1") || (mswinFlag && word.equals("/c1")))
      {
        clickReplace = true;      // each mouse click replaces sample text
      }
      else if (word.equals("-c0") || (mswinFlag && word.equals("/c0")))
        clickReplace = false;     // mouse click inserts sample text, selection

      else if (word.startsWith("-d") || (mswinFlag && word.startsWith("/d")))
        dataFile = args[i].substring(2); // accept anything for data file name

      else if (word.startsWith("-f") || (mswinFlag && word.startsWith("/f")))
        setFontName(args[i].substring(2)); // set font name for display text

      else if (word.startsWith("-s") || (mswinFlag && word.startsWith("/s")))
        setPointSize(args[i].substring(2)); // set point size for display text

      else if (word.startsWith("-u") || (mswinFlag && word.startsWith("/u")))
      {
        /* This option is followed by a font point size that will be used for
        buttons, dialogs, labels, etc. */

        int size = -1;            // default value for font point size
        try                       // try to parse remainder as unsigned integer
        {
          size = Integer.parseInt(word.substring(2));
        }
        catch (NumberFormatException nfe) // if not a number or bad syntax
        {
          size = -1;              // set result to an illegal value
        }
        if ((size < 10) || (size > 99))
        {
          System.err.println("Dialog font size must be from 10 to 99: "
            + args[i]);           // notify user of our arbitrary limits
          showHelp();             // show help summary
          System.exit(-1);        // exit application after printing help
        }
        buttonFont = new Font(SYSTEM_FONT, Font.PLAIN, size); // for big sizes
//      buttonFont = new Font(SYSTEM_FONT, Font.BOLD, size); // for small sizes
      }

      else if (word.startsWith("-w") || (mswinFlag && word.startsWith("/w")))
      {
        /* This option is followed by a list of four numbers for the initial
        window position and size. */

        Pattern pattern = Pattern.compile(
          "\\s*\\(\\s*(\\d{1,5})\\s*,\\s*(\\d{1,5})\\s*,\\s*(\\d{1,5})\\s*,\\s*(\\d{1,5})\\s*\\)\\s*");
        Matcher matcher = pattern.matcher(word.substring(2)); // parse option
        if (matcher.matches())    // if option has proper syntax
        {
          windowLeft = Integer.parseInt(matcher.group(1));
          windowTop = Integer.parseInt(matcher.group(2));
          windowWidth = Integer.parseInt(matcher.group(3));
          windowHeight = Integer.parseInt(matcher.group(4));
        }
        else                      // bad syntax or too many digits
        {
          windowHeight = windowLeft = windowTop = windowWidth = -1;
                                  // mark result as invalid
        }
        if ((windowHeight < MIN_FRAME) || (windowWidth < MIN_FRAME))
        {
          System.err.println("Invalid window position or size: " + args[i]);
          showHelp();             // show help summary
          System.exit(-1);        // exit application after printing help
        }
      }

      else if (word.equals("-x") || (mswinFlag && word.equals("/x")))
        maximizeFlag = true;      // true if we maximize our main window

      else if (word.startsWith("-") || (mswinFlag && word.startsWith("/")))
      {
        System.err.println("Option not recognized: " + args[i]);
        showHelp();               // show help summary
        System.exit(-1);          // exit application after printing help
      }

      else                        // parameter is not a recognized option
      {
        if (sampleText.length() > 0) // for second and later parameters
          sampleText += ' ';      // insert a space between parameters
        sampleText += args[i];    // append original parameter to sample text
      }
    }

    /* Open the graphical user interface (GUI).  The standard Java style is the
    most reliable, but you can switch to something closer to the local system,
    if you want. */

//  try
//  {
//    UIManager.setLookAndFeel(
//      UIManager.getCrossPlatformLookAndFeelClassName());
////    UIManager.getSystemLookAndFeelClassName());
//  }
//  catch (Exception ulafe)
//  {
//    System.err.println("Unsupported Java look-and-feel: " + ulafe);
//  }

    /* Initialize shared graphical objects. */

    setFontName(fontName);        // checks preferred name, sets <displayFont>
    userActions = new CharMap4User(null); // create our shared action listener

    /* Create the graphical interface as a series of little panels inside
    bigger panels.  The intermediate panel names are of no lasting importance
    and hence are only numbered (panel1, panel2, etc). */

    /* Create a top row for the menu button, font name and size, and caption
    text.  We put this panel inside a BorderLayout so that we can control the
    margins.*/

    JPanel panel1 = new JPanel(new BorderLayout(10, 0)); // contains top row

    JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

    menuButton = new JButton("Menu");
    menuButton.addActionListener(userActions);
    if (buttonFont != null) menuButton.setFont(buttonFont);
    menuButton.setMnemonic(KeyEvent.VK_M);
    menuButton.setToolTipText("Select characters or glyphs, close program.");
    panel2.add(menuButton);

    panel2.add(Box.createHorizontalStrut(10));

    nameDialog = new JComboBox(GraphicsEnvironment
      .getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    nameDialog.setEditable(true); // allow user to edit this dialog field
    if (buttonFont != null) nameDialog.setFont(buttonFont);
    nameDialog.setSelectedItem(fontName); // select default font name
    nameDialog.setToolTipText("Font name for display text.");
    nameDialog.addActionListener(userActions); // do last so don't fire early
    panel2.add(nameDialog);

    panel2.add(Box.createHorizontalStrut(10));

    sizeDialog = new JComboBox(FONT_SIZES); // create list from standard sizes
    sizeDialog.setEditable(false); // temporarily disable editing during layout
    if (buttonFont != null) sizeDialog.setFont(buttonFont);
    sizeDialog.setPrototypeDisplayValue("9999"); // allow for up to four digits
    sizeDialog.setToolTipText("Point size for display text.");
    panel2.add(sizeDialog);

    panel1.add(panel2, BorderLayout.WEST); // put menu, font on left side

    statusDialog = new JLabel(EMPTY_STATUS, JLabel.CENTER);
    if (buttonFont != null) statusDialog.setFont(buttonFont);
    panel1.add(statusDialog, BorderLayout.CENTER); // put status in center

    JPanel panel3 = new JPanel(new BorderLayout(0, 0));
    panel3.add(Box.createVerticalStrut(5), BorderLayout.NORTH);
    panel3.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
    panel3.add(panel1, BorderLayout.CENTER);
    panel3.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
    panel3.add(Box.createVerticalStrut(5), BorderLayout.SOUTH);

    /* This is the pop-up menu invoked by <menuButton>. */

    menuPopup = new JPopupMenu();

    menuReport = new JMenuItem("About, Help");
    menuReport.addActionListener(userActions);
    if (buttonFont != null) menuReport.setFont(buttonFont);
    menuPopup.add(menuReport);

    menuPopup.addSeparator();

    ButtonGroup group1 = new ButtonGroup();
    menuChars = new JRadioButtonMenuItem("Characters", (glyphFlag == false));
    menuChars.addActionListener(userActions);
    if (buttonFont != null) menuChars.setFont(buttonFont);
    group1.add(menuChars);
    menuPopup.add(menuChars);

    menuGlyphs = new JRadioButtonMenuItem("Glyphs", glyphFlag);
    menuGlyphs.addActionListener(userActions);
    if (buttonFont != null) menuGlyphs.setFont(buttonFont);
    group1.add(menuGlyphs);
    menuPopup.add(menuGlyphs);

    menuPopup.addSeparator();

    menuExit = new JMenuItem("Exit");
    menuExit.addActionListener(userActions);
    if (buttonFont != null) menuExit.setFont(buttonFont);
    menuExit.setMnemonic(KeyEvent.VK_X); // Alt-X normally for "Exit" button
    menuPopup.add(menuExit);

    /* This is the pop-up menu invoked by a right mouse click over a defined
    character or glyph. */

    rightPopup = new JPopupMenu();

    rightCopyCaption = new JMenuItem("Caption Text");
    rightCopyCaption.addActionListener(userActions);
    if (buttonFont != null) rightCopyCaption.setFont(buttonFont);
    rightPopup.add(rightCopyCaption);

    rightCopyNumber = new JMenuItem("Character Number");
    rightCopyNumber.addActionListener(userActions);
    if (buttonFont != null) rightCopyNumber.setFont(buttonFont);
    rightPopup.add(rightCopyNumber);

    rightCopyText = new JMenuItem("Character Text");
    rightCopyText.addActionListener(userActions);
    if (buttonFont != null) rightCopyText.setFont(buttonFont);
    rightPopup.add(rightCopyText);

    rightCopyGlyph = new JMenuItem("Glyph Number");
    rightCopyGlyph.addActionListener(userActions);
    if (buttonFont != null) rightCopyGlyph.setFont(buttonFont);
    rightPopup.add(rightCopyGlyph);

    rightCopyNotation = new JMenuItem("Unicode Notation");
    rightCopyNotation.addActionListener(userActions);
    if (buttonFont != null) rightCopyNotation.setFont(buttonFont);
    rightPopup.add(rightCopyNotation);

    /* Create a panel to display the grid of characters.  To the right of that
    is a vertical scroll bar that we control. */

    gridPanel = new CharMap4Grid(); // create display as special JPanel
    gridPanel.setFocusable(true); // allow keyboard focus for character grid

    gridScroll = new JScrollBar(JScrollBar.VERTICAL, 0, 1, 0, 1);
    gridScroll.addMouseWheelListener((MouseWheelListener) gridPanel);
    gridScroll.setEnabled(true);  // scroll bar always present, always enabled
    gridScroll.setFocusable(true); // allow keyboard focus for scroll bar
    gridScroll.getModel().addChangeListener((ChangeListener) gridPanel);

    /* Create bottom row for clear button, sample text, and copy button. */

    JPanel panel4 = new JPanel(new BorderLayout(10, 0));

    clearButton = new JButton("Clear");
    clearButton.addActionListener(userActions);
    if (buttonFont != null) clearButton.setFont(buttonFont);
    clearButton.setMnemonic(KeyEvent.VK_C);
    clearButton.setToolTipText("Clear all sample text.");
    panel4.add(clearButton, BorderLayout.WEST); // put clear on left side

    sampleDialog = new JTextField(sampleText);
    sampleDialog.addActionListener(userActions);
    sampleDialog.setFont(displayFont);
    sampleDialog.setMargin(new Insets(1, 5, 2, 5)); // top, left, bottom, right
    panel4.add(sampleDialog, BorderLayout.CENTER); // put sample in center

    copyButton = new JButton("Copy All");
    copyButton.addActionListener(userActions);
    if (buttonFont != null) copyButton.setFont(buttonFont);
    copyButton.setMnemonic(KeyEvent.VK_A);
    copyButton.setToolTipText("Copy all sample text to clipboard.");
    panel4.add(copyButton, BorderLayout.EAST); // put copy on right side

    JPanel panel5 = new JPanel(new BorderLayout(0, 0));
    panel5.add(Box.createVerticalStrut(5), BorderLayout.NORTH);
    panel5.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
    panel5.add(panel4, BorderLayout.CENTER);
    panel5.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
    panel5.add(Box.createVerticalStrut(5), BorderLayout.SOUTH);

    /* Create the main window frame for this application. */

    mainFrame = new JFrame(PROGRAM_TITLE);
    JPanel panel6 = (JPanel) mainFrame.getContentPane(); // content meets frame
    panel6.setLayout(new BorderLayout(0, 0));
    panel6.add(panel3, BorderLayout.NORTH); // menu, font, size, caption
    panel6.add(gridPanel, BorderLayout.CENTER); // character or glyph cells
    panel6.add(gridScroll, BorderLayout.EAST); // scroll bar for grid display
    if (clickReplace == false)    // omit sample text if replaced by each click
      panel6.add(panel5, BorderLayout.SOUTH); // clear, sample, copy button

    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    if (maximizeFlag) mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    mainFrame.setLocation(windowLeft, windowTop); // initial top-left corner
    mainFrame.setSize(windowWidth, windowHeight); // initial window size
//  mainFrame.setSize(612, 459);  // standard size for software screenshots
    mainFrame.validate();         // do the application window layout
    mainFrame.setVisible(true);   // show the application window

    /* The default width for editable combo boxes is much too wide for the font
    point size.  A better width is obtained by making the dialog non-editable
    and then fixing the dialog at that size, before turning editing back on. */

    sizeDialog.setMaximumSize(sizeDialog.getPreferredSize());
    sizeDialog.setEditable(true); // now allow user to edit this dialog field
    sizeDialog.setSelectedItem(String.valueOf(fontSize));
                                  // selected item is our default size
    sizeDialog.addActionListener(userActions); // do last so don't fire early

    /* Hook into the keyboard to mimic the scroll bar.  It's better to do this
    here, with mappings, than an old-style keyboard listener in CharMap4Grid,
    because then we don't have problems about which component has the focus. */

    InputMap inmap = panel6.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), ACTION_LINE_DOWN);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK), ACTION_LINE_DOWN);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), ACTION_GOTO_END);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_MASK), ACTION_GOTO_END);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ACTION_SAMPLE_CLEAR);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), ACTION_REPORT_SHOW);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.ALT_MASK), ACTION_REPORT_SHOW);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), ACTION_GOTO_HOME);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_MASK), ACTION_GOTO_HOME);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), ACTION_LINE_DOWN);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, InputEvent.CTRL_MASK), ACTION_LINE_DOWN);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), ACTION_LINE_UP);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, InputEvent.CTRL_MASK), ACTION_LINE_UP);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), ACTION_LINE_DOWN);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, InputEvent.CTRL_MASK), ACTION_LINE_DOWN);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), ACTION_LINE_UP);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, InputEvent.CTRL_MASK), ACTION_LINE_UP);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), ACTION_LINE_UP);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK), ACTION_LINE_UP);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), ACTION_PAGE_DOWN);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK), ACTION_PAGE_DOWN);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), ACTION_PAGE_UP);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_MASK), ACTION_PAGE_UP);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_MASK), ACTION_REPORT_SHOW);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), ACTION_LINE_DOWN);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK), ACTION_LINE_DOWN);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), ACTION_LINE_UP);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK), ACTION_LINE_UP);
    inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_MASK), ACTION_PROGRAM_EXIT);
    ActionMap acmap = panel6.getActionMap();
    acmap.put(ACTION_GOTO_END, new CharMap4User(ACTION_GOTO_END));
    acmap.put(ACTION_GOTO_HOME, new CharMap4User(ACTION_GOTO_HOME));
    acmap.put(ACTION_LINE_DOWN, new CharMap4User(ACTION_LINE_DOWN));
    acmap.put(ACTION_LINE_UP, new CharMap4User(ACTION_LINE_UP));
    acmap.put(ACTION_PAGE_DOWN, new CharMap4User(ACTION_PAGE_DOWN));
    acmap.put(ACTION_PAGE_UP, new CharMap4User(ACTION_PAGE_UP));
    acmap.put(ACTION_PROGRAM_EXIT, new CharMap4User(ACTION_PROGRAM_EXIT));
    acmap.put(ACTION_REPORT_SHOW, new CharMap4User(ACTION_REPORT_SHOW));
    acmap.put(ACTION_SAMPLE_CLEAR, new CharMap4User(ACTION_SAMPLE_CLEAR));

    /* It can take several seconds to load the caption strings, because there
    are so many Unicode character numbers.  Rather than delaying the user when
    this application first starts, or when the mouse first moves over a cell in
    the display grid, load the captions as a low-priority background task. */

    loader = new Thread((Runnable) userActions, "loadConfig");
                                  // re-use the same action listener as above
    loader.setPriority(Thread.MIN_PRIORITY); // low priority has less impact
    loader.start();               // run separate thread to load captions

    /* Let the graphical interface run the application now. */

    if (sampleText.length() > 0)  // were we given an initial text string?
    {
      copyText();                 // copy all sample text to clipboard
      sampleDialog.select(999999999, 999999999); // move caret to end of text
    }
    sampleDialog.requestFocusInWindow(); // give keyboard focus to sample text

  } // end of main() method

// ------------------------------------------------------------------------- //

/*
  captionGet() method

  Given an integer value for a character, return the mouse caption.  If there
  is no defined caption, construct a generic caption.
*/
  static String captionGet(int value)
  {
    StringBuffer buffer;          // faster than String for multiple appends
    String caption;               // defined caption string or <null>

    /* Convert the character number to Unicode "U+nnnn" notation. */

    buffer = new StringBuffer();  // allocate empty string buffer for result
    buffer.append(unicodeNotation(value));
    buffer.append(" = ");

    /* Append the defined caption string, or create a generic caption. */

    if (captionMap == null)       // have the caption strings been loaded?
      caption = null;             // no, follow through with generic caption
    else                          // yes, attempt to fetch defined caption
      caption = (String) captionMap.get(new Integer(value)); // may be <null>

    if (caption == null)          // was there a defined caption string?
    {
      /* No caption string has been defined for this character.  Use the name
      of the Unicode "block" or range instead, as obtained from the file:

          http://www.unicode.org/Public/UNIDATA/Blocks.txt

      Block names differ slightly from the "First" and "Last" names found in
      the UNIDATA/UnicodeData.txt file.  Whatever you may think of the block
      names, they are the official punctuation and spelling.

      The following piece of code (except for the first line) is mechanically
      generated from UNIDATA/Blocks.txt by the unpublished CharMapParse1 Java
      application.  Do not edit this code manually.

      Last updated from the 2021-01-22 UNIDATA/Blocks.txt file (14.0.0). */

      if ((value >= 0x0000) && (value <= 0x007F)) // known Unicode range?
        caption = "Basic Latin";
      else if ((value >= 0x0080) && (value <= 0x00FF))
        caption = "Latin-1 Supplement";
      else if ((value >= 0x0100) && (value <= 0x017F))
        caption = "Latin Extended-A";
      else if ((value >= 0x0180) && (value <= 0x024F))
        caption = "Latin Extended-B";
      else if ((value >= 0x0250) && (value <= 0x02AF))
        caption = "IPA Extensions";
      else if ((value >= 0x02B0) && (value <= 0x02FF))
        caption = "Spacing Modifier Letters";
      else if ((value >= 0x0300) && (value <= 0x036F))
        caption = "Combining Diacritical Marks";
      else if ((value >= 0x0370) && (value <= 0x03FF))
        caption = "Greek and Coptic";
      else if ((value >= 0x0400) && (value <= 0x04FF))
        caption = "Cyrillic";
      else if ((value >= 0x0500) && (value <= 0x052F))
        caption = "Cyrillic Supplement";
      else if ((value >= 0x0530) && (value <= 0x058F))
        caption = "Armenian";
      else if ((value >= 0x0590) && (value <= 0x05FF))
        caption = "Hebrew";
      else if ((value >= 0x0600) && (value <= 0x06FF))
        caption = "Arabic";
      else if ((value >= 0x0700) && (value <= 0x074F))
        caption = "Syriac";
      else if ((value >= 0x0750) && (value <= 0x077F))
        caption = "Arabic Supplement";
      else if ((value >= 0x0780) && (value <= 0x07BF))
        caption = "Thaana";
      else if ((value >= 0x07C0) && (value <= 0x07FF))
        caption = "NKo";
      else if ((value >= 0x0800) && (value <= 0x083F))
        caption = "Samaritan";
      else if ((value >= 0x0840) && (value <= 0x085F))
        caption = "Mandaic";
      else if ((value >= 0x0860) && (value <= 0x086F))
        caption = "Syriac Supplement";
      else if ((value >= 0x0870) && (value <= 0x089F))
        caption = "Arabic Extended-B";
      else if ((value >= 0x08A0) && (value <= 0x08FF))
        caption = "Arabic Extended-A";
      else if ((value >= 0x0900) && (value <= 0x097F))
        caption = "Devanagari";
      else if ((value >= 0x0980) && (value <= 0x09FF))
        caption = "Bengali";
      else if ((value >= 0x0A00) && (value <= 0x0A7F))
        caption = "Gurmukhi";
      else if ((value >= 0x0A80) && (value <= 0x0AFF))
        caption = "Gujarati";
      else if ((value >= 0x0B00) && (value <= 0x0B7F))
        caption = "Oriya";
      else if ((value >= 0x0B80) && (value <= 0x0BFF))
        caption = "Tamil";
      else if ((value >= 0x0C00) && (value <= 0x0C7F))
        caption = "Telugu";
      else if ((value >= 0x0C80) && (value <= 0x0CFF))
        caption = "Kannada";
      else if ((value >= 0x0D00) && (value <= 0x0D7F))
        caption = "Malayalam";
      else if ((value >= 0x0D80) && (value <= 0x0DFF))
        caption = "Sinhala";
      else if ((value >= 0x0E00) && (value <= 0x0E7F))
        caption = "Thai";
      else if ((value >= 0x0E80) && (value <= 0x0EFF))
        caption = "Lao";
      else if ((value >= 0x0F00) && (value <= 0x0FFF))
        caption = "Tibetan";
      else if ((value >= 0x1000) && (value <= 0x109F))
        caption = "Myanmar";
      else if ((value >= 0x10A0) && (value <= 0x10FF))
        caption = "Georgian";
      else if ((value >= 0x1100) && (value <= 0x11FF))
        caption = "Hangul Jamo";
      else if ((value >= 0x1200) && (value <= 0x137F))
        caption = "Ethiopic";
      else if ((value >= 0x1380) && (value <= 0x139F))
        caption = "Ethiopic Supplement";
      else if ((value >= 0x13A0) && (value <= 0x13FF))
        caption = "Cherokee";
      else if ((value >= 0x1400) && (value <= 0x167F))
        caption = "Unified Canadian Aboriginal Syllabics";
      else if ((value >= 0x1680) && (value <= 0x169F))
        caption = "Ogham";
      else if ((value >= 0x16A0) && (value <= 0x16FF))
        caption = "Runic";
      else if ((value >= 0x1700) && (value <= 0x171F))
        caption = "Tagalog";
      else if ((value >= 0x1720) && (value <= 0x173F))
        caption = "Hanunoo";
      else if ((value >= 0x1740) && (value <= 0x175F))
        caption = "Buhid";
      else if ((value >= 0x1760) && (value <= 0x177F))
        caption = "Tagbanwa";
      else if ((value >= 0x1780) && (value <= 0x17FF))
        caption = "Khmer";
      else if ((value >= 0x1800) && (value <= 0x18AF))
        caption = "Mongolian";
      else if ((value >= 0x18B0) && (value <= 0x18FF))
        caption = "Unified Canadian Aboriginal Syllabics Extended";
      else if ((value >= 0x1900) && (value <= 0x194F))
        caption = "Limbu";
      else if ((value >= 0x1950) && (value <= 0x197F))
        caption = "Tai Le";
      else if ((value >= 0x1980) && (value <= 0x19DF))
        caption = "New Tai Lue";
      else if ((value >= 0x19E0) && (value <= 0x19FF))
        caption = "Khmer Symbols";
      else if ((value >= 0x1A00) && (value <= 0x1A1F))
        caption = "Buginese";
      else if ((value >= 0x1A20) && (value <= 0x1AAF))
        caption = "Tai Tham";
      else if ((value >= 0x1AB0) && (value <= 0x1AFF))
        caption = "Combining Diacritical Marks Extended";
      else if ((value >= 0x1B00) && (value <= 0x1B7F))
        caption = "Balinese";
      else if ((value >= 0x1B80) && (value <= 0x1BBF))
        caption = "Sundanese";
      else if ((value >= 0x1BC0) && (value <= 0x1BFF))
        caption = "Batak";
      else if ((value >= 0x1C00) && (value <= 0x1C4F))
        caption = "Lepcha";
      else if ((value >= 0x1C50) && (value <= 0x1C7F))
        caption = "Ol Chiki";
      else if ((value >= 0x1C80) && (value <= 0x1C8F))
        caption = "Cyrillic Extended-C";
      else if ((value >= 0x1C90) && (value <= 0x1CBF))
        caption = "Georgian Extended";
      else if ((value >= 0x1CC0) && (value <= 0x1CCF))
        caption = "Sundanese Supplement";
      else if ((value >= 0x1CD0) && (value <= 0x1CFF))
        caption = "Vedic Extensions";
      else if ((value >= 0x1D00) && (value <= 0x1D7F))
        caption = "Phonetic Extensions";
      else if ((value >= 0x1D80) && (value <= 0x1DBF))
        caption = "Phonetic Extensions Supplement";
      else if ((value >= 0x1DC0) && (value <= 0x1DFF))
        caption = "Combining Diacritical Marks Supplement";
      else if ((value >= 0x1E00) && (value <= 0x1EFF))
        caption = "Latin Extended Additional";
      else if ((value >= 0x1F00) && (value <= 0x1FFF))
        caption = "Greek Extended";
      else if ((value >= 0x2000) && (value <= 0x206F))
        caption = "General Punctuation";
      else if ((value >= 0x2070) && (value <= 0x209F))
        caption = "Superscripts and Subscripts";
      else if ((value >= 0x20A0) && (value <= 0x20CF))
        caption = "Currency Symbols";
      else if ((value >= 0x20D0) && (value <= 0x20FF))
        caption = "Combining Diacritical Marks for Symbols";
      else if ((value >= 0x2100) && (value <= 0x214F))
        caption = "Letterlike Symbols";
      else if ((value >= 0x2150) && (value <= 0x218F))
        caption = "Number Forms";
      else if ((value >= 0x2190) && (value <= 0x21FF))
        caption = "Arrows";
      else if ((value >= 0x2200) && (value <= 0x22FF))
        caption = "Mathematical Operators";
      else if ((value >= 0x2300) && (value <= 0x23FF))
        caption = "Miscellaneous Technical";
      else if ((value >= 0x2400) && (value <= 0x243F))
        caption = "Control Pictures";
      else if ((value >= 0x2440) && (value <= 0x245F))
        caption = "Optical Character Recognition";
      else if ((value >= 0x2460) && (value <= 0x24FF))
        caption = "Enclosed Alphanumerics";
      else if ((value >= 0x2500) && (value <= 0x257F))
        caption = "Box Drawing";
      else if ((value >= 0x2580) && (value <= 0x259F))
        caption = "Block Elements";
      else if ((value >= 0x25A0) && (value <= 0x25FF))
        caption = "Geometric Shapes";
      else if ((value >= 0x2600) && (value <= 0x26FF))
        caption = "Miscellaneous Symbols";
      else if ((value >= 0x2700) && (value <= 0x27BF))
        caption = "Dingbats";
      else if ((value >= 0x27C0) && (value <= 0x27EF))
        caption = "Miscellaneous Mathematical Symbols-A";
      else if ((value >= 0x27F0) && (value <= 0x27FF))
        caption = "Supplemental Arrows-A";
      else if ((value >= 0x2800) && (value <= 0x28FF))
        caption = "Braille Patterns";
      else if ((value >= 0x2900) && (value <= 0x297F))
        caption = "Supplemental Arrows-B";
      else if ((value >= 0x2980) && (value <= 0x29FF))
        caption = "Miscellaneous Mathematical Symbols-B";
      else if ((value >= 0x2A00) && (value <= 0x2AFF))
        caption = "Supplemental Mathematical Operators";
      else if ((value >= 0x2B00) && (value <= 0x2BFF))
        caption = "Miscellaneous Symbols and Arrows";
      else if ((value >= 0x2C00) && (value <= 0x2C5F))
        caption = "Glagolitic";
      else if ((value >= 0x2C60) && (value <= 0x2C7F))
        caption = "Latin Extended-C";
      else if ((value >= 0x2C80) && (value <= 0x2CFF))
        caption = "Coptic";
      else if ((value >= 0x2D00) && (value <= 0x2D2F))
        caption = "Georgian Supplement";
      else if ((value >= 0x2D30) && (value <= 0x2D7F))
        caption = "Tifinagh";
      else if ((value >= 0x2D80) && (value <= 0x2DDF))
        caption = "Ethiopic Extended";
      else if ((value >= 0x2DE0) && (value <= 0x2DFF))
        caption = "Cyrillic Extended-A";
      else if ((value >= 0x2E00) && (value <= 0x2E7F))
        caption = "Supplemental Punctuation";
      else if ((value >= 0x2E80) && (value <= 0x2EFF))
        caption = "CJK Radicals Supplement";
      else if ((value >= 0x2F00) && (value <= 0x2FDF))
        caption = "Kangxi Radicals";
      else if ((value >= 0x2FF0) && (value <= 0x2FFF))
        caption = "Ideographic Description Characters";
      else if ((value >= 0x3000) && (value <= 0x303F))
        caption = "CJK Symbols and Punctuation";
      else if ((value >= 0x3040) && (value <= 0x309F))
        caption = "Hiragana";
      else if ((value >= 0x30A0) && (value <= 0x30FF))
        caption = "Katakana";
      else if ((value >= 0x3100) && (value <= 0x312F))
        caption = "Bopomofo";
      else if ((value >= 0x3130) && (value <= 0x318F))
        caption = "Hangul Compatibility Jamo";
      else if ((value >= 0x3190) && (value <= 0x319F))
        caption = "Kanbun";
      else if ((value >= 0x31A0) && (value <= 0x31BF))
        caption = "Bopomofo Extended";
      else if ((value >= 0x31C0) && (value <= 0x31EF))
        caption = "CJK Strokes";
      else if ((value >= 0x31F0) && (value <= 0x31FF))
        caption = "Katakana Phonetic Extensions";
      else if ((value >= 0x3200) && (value <= 0x32FF))
        caption = "Enclosed CJK Letters and Months";
      else if ((value >= 0x3300) && (value <= 0x33FF))
        caption = "CJK Compatibility";
      else if ((value >= 0x3400) && (value <= 0x4DBF))
        caption = "CJK Unified Ideographs Extension A";
      else if ((value >= 0x4DC0) && (value <= 0x4DFF))
        caption = "Yijing Hexagram Symbols";
      else if ((value >= 0x4E00) && (value <= 0x9FFF))
        caption = "CJK Unified Ideographs";
      else if ((value >= 0xA000) && (value <= 0xA48F))
        caption = "Yi Syllables";
      else if ((value >= 0xA490) && (value <= 0xA4CF))
        caption = "Yi Radicals";
      else if ((value >= 0xA4D0) && (value <= 0xA4FF))
        caption = "Lisu";
      else if ((value >= 0xA500) && (value <= 0xA63F))
        caption = "Vai";
      else if ((value >= 0xA640) && (value <= 0xA69F))
        caption = "Cyrillic Extended-B";
      else if ((value >= 0xA6A0) && (value <= 0xA6FF))
        caption = "Bamum";
      else if ((value >= 0xA700) && (value <= 0xA71F))
        caption = "Modifier Tone Letters";
      else if ((value >= 0xA720) && (value <= 0xA7FF))
        caption = "Latin Extended-D";
      else if ((value >= 0xA800) && (value <= 0xA82F))
        caption = "Syloti Nagri";
      else if ((value >= 0xA830) && (value <= 0xA83F))
        caption = "Common Indic Number Forms";
      else if ((value >= 0xA840) && (value <= 0xA87F))
        caption = "Phags-pa";
      else if ((value >= 0xA880) && (value <= 0xA8DF))
        caption = "Saurashtra";
      else if ((value >= 0xA8E0) && (value <= 0xA8FF))
        caption = "Devanagari Extended";
      else if ((value >= 0xA900) && (value <= 0xA92F))
        caption = "Kayah Li";
      else if ((value >= 0xA930) && (value <= 0xA95F))
        caption = "Rejang";
      else if ((value >= 0xA960) && (value <= 0xA97F))
        caption = "Hangul Jamo Extended-A";
      else if ((value >= 0xA980) && (value <= 0xA9DF))
        caption = "Javanese";
      else if ((value >= 0xA9E0) && (value <= 0xA9FF))
        caption = "Myanmar Extended-B";
      else if ((value >= 0xAA00) && (value <= 0xAA5F))
        caption = "Cham";
      else if ((value >= 0xAA60) && (value <= 0xAA7F))
        caption = "Myanmar Extended-A";
      else if ((value >= 0xAA80) && (value <= 0xAADF))
        caption = "Tai Viet";
      else if ((value >= 0xAAE0) && (value <= 0xAAFF))
        caption = "Meetei Mayek Extensions";
      else if ((value >= 0xAB00) && (value <= 0xAB2F))
        caption = "Ethiopic Extended-A";
      else if ((value >= 0xAB30) && (value <= 0xAB6F))
        caption = "Latin Extended-E";
      else if ((value >= 0xAB70) && (value <= 0xABBF))
        caption = "Cherokee Supplement";
      else if ((value >= 0xABC0) && (value <= 0xABFF))
        caption = "Meetei Mayek";
      else if ((value >= 0xAC00) && (value <= 0xD7AF))
        caption = "Hangul Syllables";
      else if ((value >= 0xD7B0) && (value <= 0xD7FF))
        caption = "Hangul Jamo Extended-B";
      else if ((value >= 0xD800) && (value <= 0xDB7F))
        caption = "High Surrogates";
      else if ((value >= 0xDB80) && (value <= 0xDBFF))
        caption = "High Private Use Surrogates";
      else if ((value >= 0xDC00) && (value <= 0xDFFF))
        caption = "Low Surrogates";
      else if ((value >= 0xE000) && (value <= 0xF8FF))
        caption = "Private Use Area";
      else if ((value >= 0xF900) && (value <= 0xFAFF))
        caption = "CJK Compatibility Ideographs";
      else if ((value >= 0xFB00) && (value <= 0xFB4F))
        caption = "Alphabetic Presentation Forms";
      else if ((value >= 0xFB50) && (value <= 0xFDFF))
        caption = "Arabic Presentation Forms-A";
      else if ((value >= 0xFE00) && (value <= 0xFE0F))
        caption = "Variation Selectors";
      else if ((value >= 0xFE10) && (value <= 0xFE1F))
        caption = "Vertical Forms";
      else if ((value >= 0xFE20) && (value <= 0xFE2F))
        caption = "Combining Half Marks";
      else if ((value >= 0xFE30) && (value <= 0xFE4F))
        caption = "CJK Compatibility Forms";
      else if ((value >= 0xFE50) && (value <= 0xFE6F))
        caption = "Small Form Variants";
      else if ((value >= 0xFE70) && (value <= 0xFEFF))
        caption = "Arabic Presentation Forms-B";
      else if ((value >= 0xFF00) && (value <= 0xFFEF))
        caption = "Halfwidth and Fullwidth Forms";
      else if ((value >= 0xFFF0) && (value <= 0xFFFF))
        caption = "Specials";
      else if ((value >= 0x10000) && (value <= 0x1007F))
        caption = "Linear B Syllabary";
      else if ((value >= 0x10080) && (value <= 0x100FF))
        caption = "Linear B Ideograms";
      else if ((value >= 0x10100) && (value <= 0x1013F))
        caption = "Aegean Numbers";
      else if ((value >= 0x10140) && (value <= 0x1018F))
        caption = "Ancient Greek Numbers";
      else if ((value >= 0x10190) && (value <= 0x101CF))
        caption = "Ancient Symbols";
      else if ((value >= 0x101D0) && (value <= 0x101FF))
        caption = "Phaistos Disc";
      else if ((value >= 0x10280) && (value <= 0x1029F))
        caption = "Lycian";
      else if ((value >= 0x102A0) && (value <= 0x102DF))
        caption = "Carian";
      else if ((value >= 0x102E0) && (value <= 0x102FF))
        caption = "Coptic Epact Numbers";
      else if ((value >= 0x10300) && (value <= 0x1032F))
        caption = "Old Italic";
      else if ((value >= 0x10330) && (value <= 0x1034F))
        caption = "Gothic";
      else if ((value >= 0x10350) && (value <= 0x1037F))
        caption = "Old Permic";
      else if ((value >= 0x10380) && (value <= 0x1039F))
        caption = "Ugaritic";
      else if ((value >= 0x103A0) && (value <= 0x103DF))
        caption = "Old Persian";
      else if ((value >= 0x10400) && (value <= 0x1044F))
        caption = "Deseret";
      else if ((value >= 0x10450) && (value <= 0x1047F))
        caption = "Shavian";
      else if ((value >= 0x10480) && (value <= 0x104AF))
        caption = "Osmanya";
      else if ((value >= 0x104B0) && (value <= 0x104FF))
        caption = "Osage";
      else if ((value >= 0x10500) && (value <= 0x1052F))
        caption = "Elbasan";
      else if ((value >= 0x10530) && (value <= 0x1056F))
        caption = "Caucasian Albanian";
      else if ((value >= 0x10570) && (value <= 0x105BF))
        caption = "Vithkuqi";
      else if ((value >= 0x10600) && (value <= 0x1077F))
        caption = "Linear A";
      else if ((value >= 0x10780) && (value <= 0x107BF))
        caption = "Latin Extended-F";
      else if ((value >= 0x10800) && (value <= 0x1083F))
        caption = "Cypriot Syllabary";
      else if ((value >= 0x10840) && (value <= 0x1085F))
        caption = "Imperial Aramaic";
      else if ((value >= 0x10860) && (value <= 0x1087F))
        caption = "Palmyrene";
      else if ((value >= 0x10880) && (value <= 0x108AF))
        caption = "Nabataean";
      else if ((value >= 0x108E0) && (value <= 0x108FF))
        caption = "Hatran";
      else if ((value >= 0x10900) && (value <= 0x1091F))
        caption = "Phoenician";
      else if ((value >= 0x10920) && (value <= 0x1093F))
        caption = "Lydian";
      else if ((value >= 0x10980) && (value <= 0x1099F))
        caption = "Meroitic Hieroglyphs";
      else if ((value >= 0x109A0) && (value <= 0x109FF))
        caption = "Meroitic Cursive";
      else if ((value >= 0x10A00) && (value <= 0x10A5F))
        caption = "Kharoshthi";
      else if ((value >= 0x10A60) && (value <= 0x10A7F))
        caption = "Old South Arabian";
      else if ((value >= 0x10A80) && (value <= 0x10A9F))
        caption = "Old North Arabian";
      else if ((value >= 0x10AC0) && (value <= 0x10AFF))
        caption = "Manichaean";
      else if ((value >= 0x10B00) && (value <= 0x10B3F))
        caption = "Avestan";
      else if ((value >= 0x10B40) && (value <= 0x10B5F))
        caption = "Inscriptional Parthian";
      else if ((value >= 0x10B60) && (value <= 0x10B7F))
        caption = "Inscriptional Pahlavi";
      else if ((value >= 0x10B80) && (value <= 0x10BAF))
        caption = "Psalter Pahlavi";
      else if ((value >= 0x10C00) && (value <= 0x10C4F))
        caption = "Old Turkic";
      else if ((value >= 0x10C80) && (value <= 0x10CFF))
        caption = "Old Hungarian";
      else if ((value >= 0x10D00) && (value <= 0x10D3F))
        caption = "Hanifi Rohingya";
      else if ((value >= 0x10E60) && (value <= 0x10E7F))
        caption = "Rumi Numeral Symbols";
      else if ((value >= 0x10E80) && (value <= 0x10EBF))
        caption = "Yezidi";
      else if ((value >= 0x10F00) && (value <= 0x10F2F))
        caption = "Old Sogdian";
      else if ((value >= 0x10F30) && (value <= 0x10F6F))
        caption = "Sogdian";
      else if ((value >= 0x10F70) && (value <= 0x10FAF))
        caption = "Old Uyghur";
      else if ((value >= 0x10FB0) && (value <= 0x10FDF))
        caption = "Chorasmian";
      else if ((value >= 0x10FE0) && (value <= 0x10FFF))
        caption = "Elymaic";
      else if ((value >= 0x11000) && (value <= 0x1107F))
        caption = "Brahmi";
      else if ((value >= 0x11080) && (value <= 0x110CF))
        caption = "Kaithi";
      else if ((value >= 0x110D0) && (value <= 0x110FF))
        caption = "Sora Sompeng";
      else if ((value >= 0x11100) && (value <= 0x1114F))
        caption = "Chakma";
      else if ((value >= 0x11150) && (value <= 0x1117F))
        caption = "Mahajani";
      else if ((value >= 0x11180) && (value <= 0x111DF))
        caption = "Sharada";
      else if ((value >= 0x111E0) && (value <= 0x111FF))
        caption = "Sinhala Archaic Numbers";
      else if ((value >= 0x11200) && (value <= 0x1124F))
        caption = "Khojki";
      else if ((value >= 0x11280) && (value <= 0x112AF))
        caption = "Multani";
      else if ((value >= 0x112B0) && (value <= 0x112FF))
        caption = "Khudawadi";
      else if ((value >= 0x11300) && (value <= 0x1137F))
        caption = "Grantha";
      else if ((value >= 0x11400) && (value <= 0x1147F))
        caption = "Newa";
      else if ((value >= 0x11480) && (value <= 0x114DF))
        caption = "Tirhuta";
      else if ((value >= 0x11580) && (value <= 0x115FF))
        caption = "Siddham";
      else if ((value >= 0x11600) && (value <= 0x1165F))
        caption = "Modi";
      else if ((value >= 0x11660) && (value <= 0x1167F))
        caption = "Mongolian Supplement";
      else if ((value >= 0x11680) && (value <= 0x116CF))
        caption = "Takri";
      else if ((value >= 0x11700) && (value <= 0x1174F))
        caption = "Ahom";
      else if ((value >= 0x11800) && (value <= 0x1184F))
        caption = "Dogra";
      else if ((value >= 0x118A0) && (value <= 0x118FF))
        caption = "Warang Citi";
      else if ((value >= 0x11900) && (value <= 0x1195F))
        caption = "Dives Akuru";
      else if ((value >= 0x119A0) && (value <= 0x119FF))
        caption = "Nandinagari";
      else if ((value >= 0x11A00) && (value <= 0x11A4F))
        caption = "Zanabazar Square";
      else if ((value >= 0x11A50) && (value <= 0x11AAF))
        caption = "Soyombo";
      else if ((value >= 0x11AB0) && (value <= 0x11ABF))
        caption = "Unified Canadian Aboriginal Syllabics Extended-A";
      else if ((value >= 0x11AC0) && (value <= 0x11AFF))
        caption = "Pau Cin Hau";
      else if ((value >= 0x11C00) && (value <= 0x11C6F))
        caption = "Bhaiksuki";
      else if ((value >= 0x11C70) && (value <= 0x11CBF))
        caption = "Marchen";
      else if ((value >= 0x11D00) && (value <= 0x11D5F))
        caption = "Masaram Gondi";
      else if ((value >= 0x11D60) && (value <= 0x11DAF))
        caption = "Gunjala Gondi";
      else if ((value >= 0x11EE0) && (value <= 0x11EFF))
        caption = "Makasar";
      else if ((value >= 0x11FB0) && (value <= 0x11FBF))
        caption = "Lisu Supplement";
      else if ((value >= 0x11FC0) && (value <= 0x11FFF))
        caption = "Tamil Supplement";
      else if ((value >= 0x12000) && (value <= 0x123FF))
        caption = "Cuneiform";
      else if ((value >= 0x12400) && (value <= 0x1247F))
        caption = "Cuneiform Numbers and Punctuation";
      else if ((value >= 0x12480) && (value <= 0x1254F))
        caption = "Early Dynastic Cuneiform";
      else if ((value >= 0x12F90) && (value <= 0x12FFF))
        caption = "Cypro-Minoan";
      else if ((value >= 0x13000) && (value <= 0x1342F))
        caption = "Egyptian Hieroglyphs";
      else if ((value >= 0x13430) && (value <= 0x1343F))
        caption = "Egyptian Hieroglyph Format Controls";
      else if ((value >= 0x14400) && (value <= 0x1467F))
        caption = "Anatolian Hieroglyphs";
      else if ((value >= 0x16800) && (value <= 0x16A3F))
        caption = "Bamum Supplement";
      else if ((value >= 0x16A40) && (value <= 0x16A6F))
        caption = "Mro";
      else if ((value >= 0x16A70) && (value <= 0x16ACF))
        caption = "Tangsa";
      else if ((value >= 0x16AD0) && (value <= 0x16AFF))
        caption = "Bassa Vah";
      else if ((value >= 0x16B00) && (value <= 0x16B8F))
        caption = "Pahawh Hmong";
      else if ((value >= 0x16E40) && (value <= 0x16E9F))
        caption = "Medefaidrin";
      else if ((value >= 0x16F00) && (value <= 0x16F9F))
        caption = "Miao";
      else if ((value >= 0x16FE0) && (value <= 0x16FFF))
        caption = "Ideographic Symbols and Punctuation";
      else if ((value >= 0x17000) && (value <= 0x187FF))
        caption = "Tangut";
      else if ((value >= 0x18800) && (value <= 0x18AFF))
        caption = "Tangut Components";
      else if ((value >= 0x18B00) && (value <= 0x18CFF))
        caption = "Khitan Small Script";
      else if ((value >= 0x18D00) && (value <= 0x18D7F))
        caption = "Tangut Supplement";
      else if ((value >= 0x1AFF0) && (value <= 0x1AFFF))
        caption = "Kana Extended-B";
      else if ((value >= 0x1B000) && (value <= 0x1B0FF))
        caption = "Kana Supplement";
      else if ((value >= 0x1B100) && (value <= 0x1B12F))
        caption = "Kana Extended-A";
      else if ((value >= 0x1B130) && (value <= 0x1B16F))
        caption = "Small Kana Extension";
      else if ((value >= 0x1B170) && (value <= 0x1B2FF))
        caption = "Nushu";
      else if ((value >= 0x1BC00) && (value <= 0x1BC9F))
        caption = "Duployan";
      else if ((value >= 0x1BCA0) && (value <= 0x1BCAF))
        caption = "Shorthand Format Controls";
      else if ((value >= 0x1CF00) && (value <= 0x1CFCF))
        caption = "Znamenny Musical Notation";
      else if ((value >= 0x1D000) && (value <= 0x1D0FF))
        caption = "Byzantine Musical Symbols";
      else if ((value >= 0x1D100) && (value <= 0x1D1FF))
        caption = "Musical Symbols";
      else if ((value >= 0x1D200) && (value <= 0x1D24F))
        caption = "Ancient Greek Musical Notation";
      else if ((value >= 0x1D2E0) && (value <= 0x1D2FF))
        caption = "Mayan Numerals";
      else if ((value >= 0x1D300) && (value <= 0x1D35F))
        caption = "Tai Xuan Jing Symbols";
      else if ((value >= 0x1D360) && (value <= 0x1D37F))
        caption = "Counting Rod Numerals";
      else if ((value >= 0x1D400) && (value <= 0x1D7FF))
        caption = "Mathematical Alphanumeric Symbols";
      else if ((value >= 0x1D800) && (value <= 0x1DAAF))
        caption = "Sutton SignWriting";
      else if ((value >= 0x1DF00) && (value <= 0x1DFFF))
        caption = "Latin Extended-G";
      else if ((value >= 0x1E000) && (value <= 0x1E02F))
        caption = "Glagolitic Supplement";
      else if ((value >= 0x1E100) && (value <= 0x1E14F))
        caption = "Nyiakeng Puachue Hmong";
      else if ((value >= 0x1E290) && (value <= 0x1E2BF))
        caption = "Toto";
      else if ((value >= 0x1E2C0) && (value <= 0x1E2FF))
        caption = "Wancho";
      else if ((value >= 0x1E7E0) && (value <= 0x1E7FF))
        caption = "Ethiopic Extended-B";
      else if ((value >= 0x1E800) && (value <= 0x1E8DF))
        caption = "Mende Kikakui";
      else if ((value >= 0x1E900) && (value <= 0x1E95F))
        caption = "Adlam";
      else if ((value >= 0x1EC70) && (value <= 0x1ECBF))
        caption = "Indic Siyaq Numbers";
      else if ((value >= 0x1ED00) && (value <= 0x1ED4F))
        caption = "Ottoman Siyaq Numbers";
      else if ((value >= 0x1EE00) && (value <= 0x1EEFF))
        caption = "Arabic Mathematical Alphabetic Symbols";
      else if ((value >= 0x1F000) && (value <= 0x1F02F))
        caption = "Mahjong Tiles";
      else if ((value >= 0x1F030) && (value <= 0x1F09F))
        caption = "Domino Tiles";
      else if ((value >= 0x1F0A0) && (value <= 0x1F0FF))
        caption = "Playing Cards";
      else if ((value >= 0x1F100) && (value <= 0x1F1FF))
        caption = "Enclosed Alphanumeric Supplement";
      else if ((value >= 0x1F200) && (value <= 0x1F2FF))
        caption = "Enclosed Ideographic Supplement";
      else if ((value >= 0x1F300) && (value <= 0x1F5FF))
        caption = "Miscellaneous Symbols and Pictographs";
      else if ((value >= 0x1F600) && (value <= 0x1F64F))
        caption = "Emoticons";
      else if ((value >= 0x1F650) && (value <= 0x1F67F))
        caption = "Ornamental Dingbats";
      else if ((value >= 0x1F680) && (value <= 0x1F6FF))
        caption = "Transport and Map Symbols";
      else if ((value >= 0x1F700) && (value <= 0x1F77F))
        caption = "Alchemical Symbols";
      else if ((value >= 0x1F780) && (value <= 0x1F7FF))
        caption = "Geometric Shapes Extended";
      else if ((value >= 0x1F800) && (value <= 0x1F8FF))
        caption = "Supplemental Arrows-C";
      else if ((value >= 0x1F900) && (value <= 0x1F9FF))
        caption = "Supplemental Symbols and Pictographs";
      else if ((value >= 0x1FA00) && (value <= 0x1FA6F))
        caption = "Chess Symbols";
      else if ((value >= 0x1FA70) && (value <= 0x1FAFF))
        caption = "Symbols and Pictographs Extended-A";
      else if ((value >= 0x1FB00) && (value <= 0x1FBFF))
        caption = "Symbols for Legacy Computing";
      else if ((value >= 0x20000) && (value <= 0x2A6DF))
        caption = "CJK Unified Ideographs Extension B";
      else if ((value >= 0x2A700) && (value <= 0x2B73F))
        caption = "CJK Unified Ideographs Extension C";
      else if ((value >= 0x2B740) && (value <= 0x2B81F))
        caption = "CJK Unified Ideographs Extension D";
      else if ((value >= 0x2B820) && (value <= 0x2CEAF))
        caption = "CJK Unified Ideographs Extension E";
      else if ((value >= 0x2CEB0) && (value <= 0x2EBEF))
        caption = "CJK Unified Ideographs Extension F";
      else if ((value >= 0x2F800) && (value <= 0x2FA1F))
        caption = "CJK Compatibility Ideographs Supplement";
      else if ((value >= 0x30000) && (value <= 0x3134F))
        caption = "CJK Unified Ideographs Extension G";
      else if ((value >= 0xE0000) && (value <= 0xE007F))
        caption = "Tags";
      else if ((value >= 0xE0100) && (value <= 0xE01EF))
        caption = "Variation Selectors Supplement";
      else if ((value >= 0xF0000) && (value <= 0xFFFFF))
        caption = "Supplementary Private Use Area-A";
      else if ((value >= 0x100000) && (value <= 0x10FFFF))
        caption = "Supplementary Private Use Area-B";

      /* Korean Hangul syllables are so regular that they can be re-created
      from Unicode character numbers ... if so desired.  Break into initial,
      medial (middle), and final phonetic pieces.  For the code below to be
      effective, the U+AC00 to U+D7AF range above must be commented out. */

//    else if ((value >= 0xAC00) && (value <= 0xD7A3)) // Korean Hangul range?
//    {
//      int first = value - 0xAC00; // set zero point for following calculation
//      int third = first % 28;   // index of "final" phonetic piece
//      first = first / 28;       // remove value of final piece
//      int second = first % 21;  // index of "medial" phonetic piece
//      first = first / 21;       // index of "initial" phonetic piece
//
//      caption = "Hangul Syllable " + HANGUL_NAME_INITIAL[first] + " "
//        + HANGUL_NAME_MEDIAL[second] + " " + HANGUL_NAME_FINAL[third];
//      caption = caption.trim(); // remove any unused third piece
//      String sound = HANGUL_SOUND_INITIAL[first]
//        + HANGUL_SOUND_MEDIAL[second] + HANGUL_SOUND_FINAL[third];
//      caption += " (" + sound.charAt(0) + sound.substring(1).toLowerCase()
//        + ")";                  // first "letter" may be from second piece
//    }

      /* Default to a numeric caption in decimal if nothing else found. */

      else                        // no defined caption, unknown character
        caption = "decimal " + formatComma.format(value);
    }
    buffer.append(caption);       // append selected caption to result
    return(buffer.toString());    // give caller our converted string

  } // end of captionGet() method


/*
  captionPut() method

  Save a mouse caption (string) corresponding to a character value.  Do not
  include a character number in the caption; that is added by captionGet().
*/
  static void captionPut(int value, String text)
  {
    captionMap.put(new Integer(value), text);
  }


/*
  charToString() method

  Convert an integer character number to a standard Java string (that is,
  encode the character as UTF-16 text).  This isolates one of the code
  differences between Java 1.4 and Java 5.0 inside a single common method.
*/
  static String charToString(int value)
  {
    return(String.valueOf(
//    (char) value));             // Java 1.4
      Character.toChars(value))); // Java 5.0
  }


/*
  copyText() method

  Copy all sample text to the system clipboard.  Remember the current caret
  position (selection) and restore that afterwards.
*/
  static void copyText()
  {
    int end, start;               // text positions for caret and/or selection

    end = sampleDialog.getSelectionEnd(); // remember current position in text
    start = sampleDialog.getSelectionStart();
    sampleDialog.selectAll();     // select all text in the dialog box
    sampleDialog.copy();          // place that text onto the clipboard
    sampleDialog.select(start, end); // restore previous caret position
  }


/*
  loadConfig() method

  Load configuration data from a text file in the current working directory,
  which is usually the same folder as the program's *.class files.  Should we
  encounter an error, then print a message, but continue normal execution.
  None of the file data is critical to the operation of this program.

  Please see the following web sources for the most recent Unicode mapping
  tables for regular characters:

      http://www.unicode.org/Public/UNIDATA/UCD.html
      http://www.unicode.org/Public/UNIDATA/UnicodeData.txt

  The best source for information about Chinese-Japanese-Korean ideographs is:

      http://www.unicode.org/Public/UNIDATA/Unihan.html
      http://www.unicode.org/Public/UNIDATA/Unihan.txt

  Names for the Korean Hangul syllables can be found in:

      http://www.unicode.org/Public/UNIDATA/HangulSyllableType.txt
      http://www.unicode.org/Public/UNIDATA/Jamo.txt
      http://www.iana.org/assignments/idn/kr-korean.html (best raw data file)
      http://www.unicode.org/Public/MAPPINGS/VENDORS/MICSFT/WINDOWS/CP949.TXT
*/
  static void loadConfig()
  {
    byte[] array;                 // an array for exactly one byte
    Pattern buttonPattern;        // compiled regular expression
    String caption;               // defined caption string or <null>
    char ch;                      // one character from input line
    int i;                        // index variable
    BufferedReader inputFile;     // input character stream from text file
    int length;                   // size of a string in characters
    Matcher matcher;              // pattern matcher for regular expression
    Pattern mousePattern;         // compiled regular expression
    String text;                  // one input line from file, or otherwise
    String word;                  // first command word on input line

    buttonPattern = Pattern.compile("^\\s*(\\w+)\\s*=\\s*(.*\\S)\\s*$");
    captionMap = new TreeMap();   // start without any mouse captions
    mousePattern = Pattern.compile(
        "^\\s*[Uu]\\+([0-9A-Fa-f]+)\\s*=\\s*(.*\\S)\\s*$");

    /* Open and read lines from the configuration data file. */

    try                           // catch specific and general I/O errors
    {
      inputFile = new BufferedReader(new InputStreamReader(new
        FileInputStream(dataFile), "UTF-8")); // UTF-8 encoded text file
      inputFile.mark(4);          // we may need to back up a few bytes
      i = inputFile.read();       // read byte-order marker if present
      if ((i >= 0) && (i != '\uFEFF') && (i != '\uFFFE')) // skip BOM or EOF?
        inputFile.reset();        // no, regular text, go back to beginning

      while ((text = inputFile.readLine()) != null)
      {
        /* Find the first word on the input line, which determines whether this
        is a command or a comment. */

        i = 0;                    // start from beginning of input line
        length = text.length();   // number of characters to consider
        while ((i < length) && Character.isWhitespace(text.charAt(i)))
          i ++;                   // ignore leading white space (blanks, tabs)
        if ((i >= length) || (text.charAt(i) == '#')) // blank line or comment?
          continue;               // yes, return to beginning of read loop

        /* The vast majority of data lines are Unicode mouse captions. */

        if ((matcher = mousePattern.matcher(text)).matches())
        {
          try { i = Integer.parseInt(matcher.group(1), 16); } // parse U+ hex
          catch (NumberFormatException nfe) { i = -1; } // invalidate result
          if ((i >= 0) && (i <= 0x10FFFF)) // always allow full Unicode range
            captionPut(i, matcher.group(2)); // character number looks valid
          else                    // character number can't be Unicode
            System.err.println("Unicode number out of range: " + text);
        }

        /* There may be a few optional button captions or text strings. */

        else if ((matcher = buttonPattern.matcher(text)).matches())
        {
          word = matcher.group(1).toLowerCase(); // our button or menu name
          caption = matcher.group(2); // caller's Unicode replacement string

          /* Go through a lowercase list of known buttons and menu items.  The
          external names are a simple form of our internal program names. */

          if (word.equals("aboutmenu"))
            menuReport.setText(caption);
          else if (word.equals("charmenu"))
            menuChars.setText(caption);
          else if (word.equals("clearbutton"))
            clearButton.setText(caption);
          else if (word.equals("clearcaption"))
            clearButton.setToolTipText(caption);
          else if (word.equals("copybutton"))
            copyButton.setText(caption);
          else if (word.equals("copycaption"))
            copyButton.setToolTipText(caption);
          else if (word.equals("exitmenu"))
            menuExit.setText(caption);
          else if(word.equals("fontcaption"))
            nameDialog.setToolTipText(caption);
          else if (word.equals("glyphmenu"))
            menuGlyphs.setText(caption);
          else if (word.equals("menubutton"))
            menuButton.setText(caption);
          else if (word.equals("menucaption"))
            menuButton.setToolTipText(caption);
          else if (word.equals("programtitle")) // maybe this should be hidden?
            mainFrame.setTitle(caption);
          else if (word.equals("rightcaption"))
            rightCopyCaption.setText(caption);
          else if (word.equals("rightglyph"))
            rightCopyGlyph.setText(caption);
          else if (word.equals("rightnotation"))
            rightCopyNotation.setText(caption);
          else if (word.equals("rightnumber"))
            rightCopyNumber.setText(caption);
          else if (word.equals("righttext"))
            rightCopyText.setText(caption);
          else if (word.equals("sizecaption"))
            sizeDialog.setToolTipText(caption);
          else
            System.err.println("Unknown button or menu name: " + text);
        }

        /* Warn the user about an invalid command line in the data file. */

        else
        {
          System.err.println("Unknown configuration command: " + text);
        }
      }
      inputFile.close();          // try to close input file
    }

    catch (FileNotFoundException fnfe) // if our data file does not exist
    {
      /* Put special code here if you want to ignore the missing file. */

      if (dataFile.equals(DEFAULT_FILE) == false)
      {
        System.err.println("Configuration data file not found: " + dataFile);
        System.err.println("in current working directory "
          + System.getProperty("user.dir"));
      }

      /* Supply default names for the plain text ASCII characters only. */

      captionPut(0x0020, "Space");
      captionPut(0x0021, "Exclamation Mark");
      captionPut(0x0022, "Quotation Mark");
      captionPut(0x0023, "Number Sign");
      captionPut(0x0024, "Dollar Sign");
      captionPut(0x0025, "Percent Sign");
      captionPut(0x0026, "Ampersand");
      captionPut(0x0027, "Apostrophe");
      captionPut(0x0028, "Left Parenthesis");
      captionPut(0x0029, "Right Parenthesis");
      captionPut(0x002A, "Asterisk");
      captionPut(0x002B, "Plus Sign");
      captionPut(0x002C, "Comma");
      captionPut(0x002D, "Hyphen-Minus");
      captionPut(0x002E, "Full Stop");
      captionPut(0x002F, "Solidus");
      captionPut(0x0030, "Digit Zero");
      captionPut(0x0031, "Digit One");
      captionPut(0x0032, "Digit Two");
      captionPut(0x0033, "Digit Three");
      captionPut(0x0034, "Digit Four");
      captionPut(0x0035, "Digit Five");
      captionPut(0x0036, "Digit Six");
      captionPut(0x0037, "Digit Seven");
      captionPut(0x0038, "Digit Eight");
      captionPut(0x0039, "Digit Nine");
      captionPut(0x003A, "Colon");
      captionPut(0x003B, "Semicolon");
      captionPut(0x003C, "Less-Than Sign");
      captionPut(0x003D, "Equals Sign");
      captionPut(0x003E, "Greater-Than Sign");
      captionPut(0x003F, "Question Mark");
      captionPut(0x0040, "Commercial At");
      captionPut(0x0041, "Latin Capital Letter A");
      captionPut(0x0042, "Latin Capital Letter B");
      captionPut(0x0043, "Latin Capital Letter C");
      captionPut(0x0044, "Latin Capital Letter D");
      captionPut(0x0045, "Latin Capital Letter E");
      captionPut(0x0046, "Latin Capital Letter F");
      captionPut(0x0047, "Latin Capital Letter G");
      captionPut(0x0048, "Latin Capital Letter H");
      captionPut(0x0049, "Latin Capital Letter I");
      captionPut(0x004A, "Latin Capital Letter J");
      captionPut(0x004B, "Latin Capital Letter K");
      captionPut(0x004C, "Latin Capital Letter L");
      captionPut(0x004D, "Latin Capital Letter M");
      captionPut(0x004E, "Latin Capital Letter N");
      captionPut(0x004F, "Latin Capital Letter O");
      captionPut(0x0050, "Latin Capital Letter P");
      captionPut(0x0051, "Latin Capital Letter Q");
      captionPut(0x0052, "Latin Capital Letter R");
      captionPut(0x0053, "Latin Capital Letter S");
      captionPut(0x0054, "Latin Capital Letter T");
      captionPut(0x0055, "Latin Capital Letter U");
      captionPut(0x0056, "Latin Capital Letter V");
      captionPut(0x0057, "Latin Capital Letter W");
      captionPut(0x0058, "Latin Capital Letter X");
      captionPut(0x0059, "Latin Capital Letter Y");
      captionPut(0x005A, "Latin Capital Letter Z");
      captionPut(0x005B, "Left Square Bracket");
      captionPut(0x005C, "Reverse Solidus");
      captionPut(0x005D, "Right Square Bracket");
      captionPut(0x005E, "Circumflex Accent");
      captionPut(0x005F, "Low Line");
      captionPut(0x0060, "Grave Accent");
      captionPut(0x0061, "Latin Small Letter A");
      captionPut(0x0062, "Latin Small Letter B");
      captionPut(0x0063, "Latin Small Letter C");
      captionPut(0x0064, "Latin Small Letter D");
      captionPut(0x0065, "Latin Small Letter E");
      captionPut(0x0066, "Latin Small Letter F");
      captionPut(0x0067, "Latin Small Letter G");
      captionPut(0x0068, "Latin Small Letter H");
      captionPut(0x0069, "Latin Small Letter I");
      captionPut(0x006A, "Latin Small Letter J");
      captionPut(0x006B, "Latin Small Letter K");
      captionPut(0x006C, "Latin Small Letter L");
      captionPut(0x006D, "Latin Small Letter M");
      captionPut(0x006E, "Latin Small Letter N");
      captionPut(0x006F, "Latin Small Letter O");
      captionPut(0x0070, "Latin Small Letter P");
      captionPut(0x0071, "Latin Small Letter Q");
      captionPut(0x0072, "Latin Small Letter R");
      captionPut(0x0073, "Latin Small Letter S");
      captionPut(0x0074, "Latin Small Letter T");
      captionPut(0x0075, "Latin Small Letter U");
      captionPut(0x0076, "Latin Small Letter V");
      captionPut(0x0077, "Latin Small Letter W");
      captionPut(0x0078, "Latin Small Letter X");
      captionPut(0x0079, "Latin Small Letter Y");
      captionPut(0x007A, "Latin Small Letter Z");
      captionPut(0x007B, "Left Curly Bracket");
      captionPut(0x007C, "Vertical Line");
      captionPut(0x007D, "Right Curly Bracket");
      captionPut(0x007E, "Tilde");
    }

    catch (IOException ioe)       // for all other file I/O errors
    {
      System.err.println("Unable to read configuration data from file "
        + dataFile);
      System.err.println("in current working directory "
        + System.getProperty("user.dir"));
      System.err.println(ioe.getMessage());
    }

    /* Windows has a pre-defined way of entering non-keyboard characters up to
    0xFF or decimal 255: hold down the Alt key and press exactly four digits
    from 0000 to 0255 on the numeric keypad with NumLock on.  This will use the
    system's default character set encoding.  (Three digits are interpreted
    with an older MS-DOS character set.)  The Alt+nnnn numbers are helpful, but
    since they vary from locale to locale, they must be re-generated here, and
    can not be fixed as part of the regular caption strings.  Dingbat fonts
    that use the C1 control region (0x80 to 0x9F) may not receive correct Alt+
    numbers for that region.  This is unavoidable because the C1 control region
    has shift codes for many double-byte character sets. */

    if (mswinFlag)                // only if running on Microsoft Windows
    {
      array = new byte[1];        // test one encoded byte at a time
      for (i = 0x20; i <= 0xFF; i ++) // do all non-control 8-bit bytes
      {
        array[0] = (byte) i;      // construct byte array for decoding
        text = new String(array); // convert byte to Unicode, or replace char
        if ((text.length() == 1) && ((ch = text.charAt(0)) != REPLACE_CHAR))
        {
          caption = (String) captionMap.get(new Integer((int) ch));
                                  // fetch actual value, not via <captionGet>
          if (caption == null)    // but have we already created a caption?
            caption = "";         // no, use empty string, nothing to append to
          else                    // yes, there is a caption and we are adding
            caption += " = ";     // insert delimiter between caption and Alt+
          caption += winaltNotation(i); // append Windows Alt+nnnn key code
          captionPut((int) ch, caption); // save new caption string
        }
      } // end of <for> loop
    }
  } // end of loadConfig() method


/*
  putError() method

  Show an error message to the user, either printed or as a pop-up dialog.
*/
  static void putError(String text)
  {
    if (mainFrame == null)        // during setup, there is no GUI window
      System.err.println(text);   // write directly onto Java console
    else
      JOptionPane.showMessageDialog(mainFrame, text); // pop-up dialog for GUI
  }


/*
  reportMakeDialog() method

  Create (but not show) the dialog box used both for displaying this program's
  license and for showing a summary of characters currently in the sample text.
  Someone else must fill in the text area and call reportShowDialog() to make
  the dialog box visible.

  We re-use the same JFrame object for all calls to this method.  A JFrame is
  better here than a JDialog, because a JFrame can be minimized or maximized,
  and a JFrame can be placed under the main window.
*/
  static void reportMakeDialog()
  {
    if (reportDialog != null)     // have we already created the dialog?
      return;                     // yes, then there is nothing more to do

    /* Create the basic window frame.  We don't set a title string here, since
    that will be done later by each different report.  The position and size of
    the window are relative to the initial size of the main window frame. */

    reportDialog = new JFrame();  // title will be added later
    reportDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    reportDialog.setLocation(windowLeft + 30, windowTop + 30);
    reportDialog.setSize(windowWidth - 60, windowHeight - 60);

    /* Make it easy for the user to close this dialog with the Escape key. */

    JPanel panel1 = (JPanel) reportDialog.getContentPane();
    panel1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
      KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ACTION_REPORT_HIDE);
    panel1.getActionMap().put(ACTION_REPORT_HIDE,
      new CharMap4User(ACTION_REPORT_HIDE)); // hide window on Escape key

    /* Create a scrolling text area for output from different reports. */

    reportText = new JTextArea(); // create panel for writing report
    reportText.setEditable(false); // user can't change this text area
    reportText.setFont(new Font(SYSTEM_FONT, Font.PLAIN, 18)); // text font
    reportText.setLineWrap(false); // don't wrap text lines
    reportText.setOpaque(false);  // transparent background, not white
    JScrollPane panel2 = new JScrollPane(reportText); // add scroll bars
    panel2.setBorder(javax.swing.BorderFactory.createEmptyBorder());

    /* Create a horizontal panel for the action buttons. */

    JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

    reportCloseButton = new JButton("Close");
    reportCloseButton.addActionListener(userActions);
    if (buttonFont != null) reportCloseButton.setFont(buttonFont);
    reportCloseButton.setMnemonic(KeyEvent.VK_C);
    reportCloseButton.setToolTipText("Close/hide this dialog window.");
    panel3.add(reportCloseButton);
    panel3.add(Box.createHorizontalStrut(40));

    reportLicenseButton = new JButton("Show License");
    reportLicenseButton.addActionListener(userActions);
    if (buttonFont != null) reportLicenseButton.setFont(buttonFont);
    reportLicenseButton.setMnemonic(KeyEvent.VK_L);
    reportLicenseButton.setToolTipText("Show user license for this program.");
    panel3.add(reportLicenseButton);
    panel3.add(Box.createHorizontalStrut(40));

    reportSummaryButton = new JButton("Show Summary");
    reportSummaryButton.addActionListener(userActions);
    if (buttonFont != null) reportSummaryButton.setFont(buttonFont);
    reportSummaryButton.setMnemonic(KeyEvent.VK_S);
    reportSummaryButton.setToolTipText("Show program summary, sample text.");
    panel3.add(reportSummaryButton);

    /* Put the text in the center of a BorderLayout, with the buttons below. */

    JPanel panel4 = new JPanel(new BorderLayout(10, 10));
    panel4.add(panel2, BorderLayout.CENTER); // text area
    panel4.add(panel3, BorderLayout.SOUTH); // action buttons

    /* Use another BorderLayout for precise control over the margins. */

    panel1.setLayout(new BorderLayout(0, 0)); // content pane from JFrame
    panel1.add(Box.createVerticalStrut(15), BorderLayout.NORTH);
    panel1.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
    panel1.add(panel4, BorderLayout.CENTER);
    panel1.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
    panel1.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);

  } // end of reportMakeDialog() method


/*
  reportShowDialog() method

  After another method has set the text area, show the report dialog window.
*/
  static void reportShowDialog()
  {
    int state = reportDialog.getExtendedState(); // normal? maximize? minimize?
    if ((state & JFrame.ICONIFIED) != 0) // is dialog currently minimized?
      reportDialog.setExtendedState(state ^ JFrame.ICONIFIED); // yes, restore
    reportDialog.validate();      // redo the layout for this dialog window
    reportDialog.setVisible(true); // show the window or bring it to the front
    reportDialog.repaint();       // window may need to redraw after minimize
    reportCloseButton.requestFocusInWindow(); // give focus to Close button
  }


/*
  reportShowLicense() method

  Display this program's license by reading from an assumed plain text file.
*/
  static void reportShowLicense()
  {
    reportMakeDialog();           // create the common dialog if necessary
    reportDialog.setTitle(LICENSE_NAME); // use license name as window title
    try { reportText.read(new FileReader(LICENSE_FILE), null); } // load text
    catch (IOException ioe)       // includes FileNotFoundException
    {
      reportText.setText("Sorry, can't read license text from file: "
        + LICENSE_FILE);
    }
    reportText.select(0, 0);      // force text display back to beginning
    reportShowDialog();           // show the report dialog window
  }


/*
  reportShowSummary() method

  Show information about this program and the user's current sample text.
*/
  static void reportShowSummary()
  {
    int ch;                       // one character from string as an integer
    int i;                        // index variable
    String input;                 // extracted string from sample text box
    int length;                   // size of input string in characters

    reportMakeDialog();           // create the common dialog if necessary
    reportDialog.setTitle("Characters in Sample Text"); // window title

    input = sampleDialog.getText(); // get characters from sample text box
    length = input.length();      // get total number of sample characters
    reportText.setText(PROGRAM_TITLE + "\n" + COPYRIGHT_NOTICE + "\n\n" +
      "Current window position is (" + mainFrame.getX() + ","
      + mainFrame.getY() + ") and size is (" + mainFrame.getWidth()
      + "," + mainFrame.getHeight() + ") pixels.\n\nFont <" + fontName
      + "> has " + formatComma.format(gridPanel.charCount)
      + " characters with " + formatComma.format(gridPanel.glyphCount)
      + " glyphs.\n");

//  i = length;                   // number of real characters // Java 1.4
    i = Character.codePointCount(input, 0, length); // Java 5.0
    if (i > 1)                    // format a pretty singular/plural message
      reportText.append("Sample text has " + formatComma.format(i)
        + " characters:\n");
    else if (i > 0)               // more than zero but less than two
      reportText.append("Sample text has one character:\n");
    else                          // less than one, hence zero
      reportText.append("Sample text is empty.\n");

    i = 0;                        // start from beginning of string
    while (i < length)            // do all characters in the sample text
    {
//    ch = (int) input.charAt(i ++); // get one standard character // Java 1.4
      ch = Character.codePointAt(input, i); // get extended char // Java 5.0
      i += Character.charCount(ch); // increment index by one or 2 // Java 5.0
      reportText.append(charToString(ch) + " = " + captionGet(ch) + "\n");
    }

    reportText.select(0, 0);      // force text display back to beginning
    reportShowDialog();           // show the report dialog window

  } // end of reportShowSummary() method


/*
  setDisplayFont() method

  This method is called after either the font name or the point size changes
  for display text.
*/
  static void setDisplayFont()
  {
    displayFont = new Font(fontName, Font.PLAIN, fontSize);
    if (mainFrame != null)        // is the GUI up and running yet?
    {
      sampleDialog.setFont(displayFont); // apply new font to sample text
      mainFrame.validate();       // redo the application window layout
      gridPanel.clear();          // display characters from the beginning
    }
  }


/*
  setFontName() method

  The caller gives us a preferred font name for display text.  We use that font
  if it's available.  Otherwise, we default to the local system font.
*/
  static void setFontName(String text)
  {
    fontName = text;              // assume name is valid, remember this name
    if (fontName.equals((new Font(fontName, Font.PLAIN, fontSize)).getFamily()))
    {
      /* This is a valid font name.  No changes are required. */
    }
    else                          // can't find requested font
    {
      putError("Font name <" + fontName + "> not found; using <" + SYSTEM_FONT
        + "> instead.");
      fontName = SYSTEM_FONT;     // replace with standard system font
      if (mainFrame != null)      // is the GUI up and running yet?
        nameDialog.setSelectedItem(fontName); // reset dialog
    }
    setDisplayFont();             // redo the layout with the new font or size

  } // end of setFontName() method


/*
  setPointSize() method

  The caller gives us a preferred point size for display text, as a string.  We
  use that size if it's available.  Otherwise, we default to our initial size.
*/
  static void setPointSize(String text)
  {
    try                           // try to parse parameter as an integer
    {
      fontSize = Integer.parseInt(text); // return signed integer, or exception
    }
    catch (NumberFormatException nfe) // if not a number or bad syntax
    {
      fontSize = -1;              // mark result as invalid
    }
    if ((fontSize >= MIN_SIZE) && (fontSize <= MAX_SIZE))
    {
      /* This is a valid point size.  No changes are required. */
    }
    else                          // given point size was out of range
    {
      putError("Point size <" + text + "> must be from " + MIN_SIZE + " to "
        + MAX_SIZE + "; using " + DEFAULT_SIZE + " instead.");
      fontSize = DEFAULT_SIZE;    // default point size for display text
      if (mainFrame != null)      // is the GUI up and running yet?
        sizeDialog.setSelectedItem(String.valueOf(fontSize)); // reset dialog
    }
    setDisplayFont();             // redo the layout with the new font or size

  } // end of setPointSize() method


/*
  showHelp() method

  Show the help summary.  This is a UNIX standard and is expected for all
  console applications, even very simple ones.
*/
  static void showHelp()
  {
    System.err.println();
    System.err.println(PROGRAM_TITLE);
    System.err.println();
    System.err.println("This is a graphical application.  You may give options and/or sample text on");
    System.err.println("the command line.  Options are:");
    System.err.println();
    System.err.println("Options:");
    System.err.println("  -? = -help = show summary of command-line syntax");
    System.err.println("  -c0 = mouse click inserts sample text or replaces selection (default)");
    System.err.println("  -c1 = -c = each mouse click replaces sample text with one character");
    System.err.println("  -d# = text file with character name data; default is -d\"" + DEFAULT_FILE + "\"");
    System.err.println("  -f# = initial font for display text; default is -f\"" + DEFAULT_FONT + "\"");
    System.err.println("  -s# = initial point size for display text; default is -s" + DEFAULT_SIZE);
    System.err.println("  -u# = font size for buttons, dialogs, etc; default is local system;");
    System.err.println("      example: -u16");
    System.err.println("  -w(#,#,#,#) = normal window position: left, top, width, height;");
    System.err.println("      default is -w(" + DEFAULT_LEFT + "," + DEFAULT_TOP + ","
      + DEFAULT_WIDTH + "," + DEFAULT_HEIGHT + ")");
    System.err.println("  -x = maximize application window; default is normal window");
    System.err.println();
    System.err.println(COPYRIGHT_NOTICE);
//  System.err.println();

  } // end of showHelp() method


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


/*
  userButton() method

  This method is called by our action listener actionPerformed() to process
  buttons, in the context of the main CharMap4 class.
*/
  static void userButton(ActionEvent event)
  {
    Object source = event.getSource(); // where the event came from
    if (source == clearButton)    // "Clear" button
    {
      sampleDialog.setText("");   // erase all sample text
      sampleDialog.requestFocusInWindow(); // give keyboard focus to sample text
    }
    else if (source == copyButton) // "Copy All" button
    {
      copyText();                 // copy all sample text to clipboard
      sampleDialog.requestFocusInWindow(); // give keyboard focus to sample text
    }
    else if (source == menuButton) // generic "Menu" button
    {
      menuPopup.show(menuButton, 0, menuButton.getHeight());
    }
    else if (source == menuChars) // "Show Characters" button or menu item
    {
      glyphFlag = ! menuChars.isSelected(); // show glyphs if not characters
      gridPanel.clickIndex = gridPanel.hoverIndex = gridPanel.NO_MOUSE;
                                  // cancel forgotten highlight character
      gridPanel.cornerIndex = 0;  // force top-left corner to first character
      gridPanel.repaint();        // mark ourselves as needing to be repainted
    }
    else if (source == menuExit)  // "Exit" button or menu item
    {
      System.exit(0);             // always exit with zero status from GUI
    }
    else if (source == menuGlyphs) // "Show Glyphs" button or menu item
    {
      glyphFlag = menuGlyphs.isSelected(); // show glyphs if glyphs selected
      gridPanel.clickIndex = gridPanel.hoverIndex = gridPanel.NO_MOUSE;
                                  // cancel forgotten highlight character
      gridPanel.cornerIndex = 0;  // force top-left corner to first character
      gridPanel.repaint();        // mark ourselves as needing to be repainted
    }
    else if (source == menuReport) // "Report" button or menu item
    {
      reportShowSummary();        // show summary of characters in sample text
    }
    else if (source == nameDialog) // font name for display text
    {
      setFontName((String) nameDialog.getSelectedItem());
    }
    else if (source == rightCopyCaption) // right click: "Caption Text"
    {
      if ((rightSaveCaption != null) && (rightSaveCaption.length() > 0))
        gridPanel.mouseReplaceText(rightSaveCaption);
    }
    else if (source == rightCopyGlyph) // right click: "Glyph Number"
    {
      if (rightSaveGlyph >= 0)
        gridPanel.mouseReplaceText(String.valueOf(rightSaveGlyph));
    }
    else if (source == rightCopyNotation) // right click: "Unicode Notation"
    {
      if (rightSaveChar >= 0)
        gridPanel.mouseReplaceText(unicodeNotation(rightSaveChar));
    }
    else if (source == rightCopyNumber) // right click: "Character Number"
    {
      if (rightSaveChar >= 0)
        gridPanel.mouseReplaceText(String.valueOf(rightSaveChar));
    }
    else if (source == rightCopyText) // right click: "Character Text"
    {
      if (rightSaveChar >= 0)
        gridPanel.mouseReplaceText(charToString(rightSaveChar));
    }
    else if (source == sampleDialog) // pressing Enter key on sample text
    {
      copyButton.doClick();       // simulate user click on "Copy All" button
    }
    else if (source == sizeDialog) // point size for display text
    {
      setPointSize((String) sizeDialog.getSelectedItem());
    }

    /* These buttons are only valid if the report dialog has been created. */

    else if ((reportDialog != null) && (source == reportCloseButton))
    {
      reportDialog.setVisible(false); // hide report dialog, don't close window
    }
    else if ((reportDialog != null) && (source == reportLicenseButton))
    {
      reportShowLicense();        // show user license text for this program
    }
    else if ((reportDialog != null) && (source == reportSummaryButton))
    {
      reportShowSummary();        // show summary of characters in sample text
    }

    /* Programming error: unknown GUI element invoked this action listener. */

    else                          // fault in program logic, not by user
    {
      System.err.println("Error in userButton(): unknown ActionEvent: "
        + event);                 // should never happen, so write on console
    }
  } // end of userButton() method


/*
  userKey() method

  The caller gives us a command string for a keyboard action.  The only actions
  currently defined are to mimic the scroll bar or menu items.
*/
  static void userKey(String command)
  {
    if (mainFrame == null)
    {
      /* Do nothing because the GUI hasn't been set up yet. */
    }
    else if (command.equals(ACTION_GOTO_END))
    {
      gridScroll.setValue(gridScroll.getMaximum());
    }
    else if (command.equals(ACTION_GOTO_HOME))
    {
      gridScroll.setValue(gridScroll.getMinimum());
    }
    else if (command.equals(ACTION_LINE_DOWN))
    {
      gridScroll.setValue(gridScroll.getValue() + 1);
    }
    else if (command.equals(ACTION_LINE_UP))
    {
      gridScroll.setValue(gridScroll.getValue() - 1);
    }
    else if (command.equals(ACTION_PAGE_DOWN))
    {
      gridScroll.setValue(gridScroll.getValue()
        + gridScroll.getBlockIncrement());
    }
    else if (command.equals(ACTION_PAGE_UP))
    {
      gridScroll.setValue(gridScroll.getValue()
        - gridScroll.getBlockIncrement());
    }
    else if (command.equals(ACTION_PROGRAM_EXIT))
    {
      menuExit.doClick();         // same action as selecting menu item
    }
    else if (command.equals(ACTION_REPORT_HIDE))
    {
      reportDialog.setVisible(false); // hide report dialog, don't close window
    }
    else if (command.equals(ACTION_REPORT_SHOW))
    {
      menuReport.doClick();       // same action as selecting menu item
    }
    else if (command.equals(ACTION_SAMPLE_CLEAR))
    {
      clearButton.doClick();      // simulate user click on "Clear" button
    }
    else                          // fault in program logic, not by user
    {
      System.err.println("Error in userKey(): unknown command: "
        + command);               // should never happen, so write on console
    }
  } // end of userKey() method


/*
  winaltNotation() method

  Given an integer, return the Windows "Alt+nnnn" notation for that character
  number.  Valid range is only from 0032 to 0255 decimal.
*/
  static String winaltNotation(int value)
  {
    String result;                // our converted result

    result = Integer.toString(value); // convert binary to decimal
    if (result.length() < 4)      // must have at least four digits
      result = "0000".substring(result.length()) + result;
    result = "Alt+" + result;     // insert the "Alt+" prefix

    return(result);               // give caller our converted string
  }

} // end of CharMap4 class

// ------------------------------------------------------------------------- //

/*
  CharMap4Grid class

  This class draws the character grid and listens for mouse and scroll events.
  Keyboard events are handled by the main class and mimic the scroll bar.
*/

class CharMap4Grid extends JPanel implements ChangeListener, MouseListener,
  MouseMotionListener, MouseWheelListener
{
  /* constants */

  static final Color GRID_COLOR = Color.LIGHT_GRAY; // color of boxes, lines
  static final int GRID_WIDTH = 2; // width of grid lines in pixels
  static final int MOUSE_DRIFT = 10; // pixel movement allowed on mouse click
  static final int NO_MOUSE = -1; // index when mouse not on defined character
  static final Color PANEL_COLOR = Color.WHITE; // normal background color
  static final int PANEL_MARGIN = 5; // outside margin of panel in pixels
  static final Color TEXT_COLOR = Color.BLACK; // color of all display text
  static final int TEXT_MARGIN = 4; // margin inside cell for each character

  /* instance variables */

  int clickIndex;                 // cell index of clicked character
  int clickStartX, clickStartY;   // starting pixel coordinates of mouse click
  int cornerIndex;                // cell index of top-left corner
  FontMetrics fontData;           // information about current display font
  int horizStep;                  // horizontal offset from one cell to next
  int hoverIndex;                 // cell index of mouse over character
  int lineAscent;                 // number of pixels above baseline
  int lineHeight;                 // height of each display line in pixels
  int maxWidth;                   // maximum pixel width of all characters
  int panelColumns;               // number of complete text columns displayed
  int panelCount;                 // saved value of <cellCount> used previously
  Font panelFont;                 // saved font for drawing text on this panel
  int panelHeight, panelWidth;    // saved panel height and width in pixels
  int panelRows;                  // number of complete lines (rows) displayed
  int vertiStep;                  // vertical offset from one cell to next

  /* To switch between displaying characters and displaying raw glyphs, we
  create identical information structures for both, and switch only object
  references.  A "cell" is our generic name for either one or the other. */

  int[] cellChar;                 // unique character number, or -1
  int cellCount;                  // number of displayed characters or glyphs
  int[] cellGlyph;                // unique glyph number, or -1

  int[] charChar;                 // as above, but for Unicode characters
  int charCount;
  int[] charGlyph;

  int[] glyphChar;                // as above, but for raw glyph numbers
  int glyphCount;
  int[] glyphGlyph;

  /* class constructor */

  public CharMap4Grid()
  {
    super();                      // initialize our superclass first (JPanel)

    /* Set class instance variables to undefined values that we will recognize
    if we are called before the layout and first "paint" is complete. */

    cellCount = charCount = glyphCount = 0; // no chars or glyphs to display
    clickIndex = NO_MOUSE;        // cell index of clicked character
    clickStartX = clickStartY = NO_MOUSE; // no starting coordinates for click
    cornerIndex = 0;              // cell index of top-left corner
    fontData = null;              // information about current display font
    horizStep = 100;              // horizontal offset from one cell to next
    hoverIndex = NO_MOUSE;        // cell index of mouse over character
    lineAscent = 100;             // number of pixels above baseline
    lineHeight = 100;             // height of each display line in pixels
    maxWidth = 100;               // maximum pixel width of all characters
    panelColumns = 10;            // number of complete text columns displayed
    panelCount = -1;              // saved value of <cellCount> used previously
    panelFont = null;             // saved font for drawing text on this panel
    panelHeight = panelWidth = -1; // saved panel height and width in pixels
    panelRows = 10;               // number of complete lines (rows) displayed
    vertiStep = 100;              // vertical offset from one cell to next

    /* Install our mouse and scroll listeners. */

    this.addMouseListener((MouseListener) this);
    this.addMouseMotionListener((MouseMotionListener) this);
    this.addMouseWheelListener((MouseWheelListener) this);
//  this.setFocusable(false);     // we don't handle keyboard input, owner does

  } // end of CharMap4Grid() constructor


/*
  clear() method

  The caller wants us to initialize the display, from the beginning.  Flag our
  class variables so that this will happen when the panel is next redrawn.
*/
  void clear()
  {
    panelFont = null;             // saved font for drawing text on this panel
    this.repaint();               // mark ourselves as needing to be repainted
  }


/*
  convertMouse() method

  Convert mouse coordinates to a cell index.  Return <NO_MOUSE> if the mouse is
  not well-centered on a defined character.
*/
  int convertMouse(MouseEvent event)
  {
    int result;                   // converted cell index or <NO_MOUSE>

    if (panelFont == null)        // can't do conversion if we haven't painted
      return(NO_MOUSE);           // tell caller to come back some other time

    int colOff = event.getX() - GRID_WIDTH - PANEL_MARGIN; // known margins
    int colNum = colOff / horizStep; // convert pixels to column number
    int colRem = colOff % horizStep; // how far inside cell is the mouse?
    if ((colNum >= panelColumns) || (colRem < TEXT_MARGIN)
      || (colRem > (horizStep - GRID_WIDTH - TEXT_MARGIN)))
    {
      return(NO_MOUSE);           // horizontal coordinate is out of range
    }

    int rowOff = event.getY() - GRID_WIDTH - PANEL_MARGIN; // known margins
    int rowNum = rowOff / vertiStep; // convert pixels to row number
    int rowRem = rowOff % vertiStep; // how far inside cell is the mouse?
    if ((rowRem < TEXT_MARGIN)
      || (rowRem > (vertiStep - GRID_WIDTH - TEXT_MARGIN)))
    {
      return(NO_MOUSE);           // vertical coordinate is out of range
    }

    result = cornerIndex + (rowNum * panelColumns) + colNum;
    if (result >= cellCount)      // is the mouse beyond the last character?
      return(NO_MOUSE);           // character index is out of range

    return(result);               // give caller a valid character index

  } // end of convertMouse() method


/*
  mouseClicked(), mouseDragged(), ..., mouseReleased() methods

  These are the mouse click and movement listeners.
*/
  public void mouseClicked(MouseEvent event) { /* See mouseReleased(). */ }

  public void mouseDragged(MouseEvent event)
  {
    mouseMoved(event);            // treat click-and-drag as simple movement
  }

  public void mouseEntered(MouseEvent event) { /* not used */ }

  public void mouseExited(MouseEvent event) { /* not used */ }

  public void mouseMoved(MouseEvent event)
  {
    /* Called when the mouse changes position, often pixel by pixel.  We only
    use this for a mouse caption, which doesn't actually follow the mouse, as
    that would be annoying.  The text appears in a fixed location at the top of
    the screen.  Mouse movement can change a "click" into a "drag".  We don't
    automatically cancel our click highlighting upon movement, because some
    tolerance is more comfortable for users. */

    StringBuffer buffer;          // faster than String for multiple appends
    int ch;                       // one character from string as an integer
    int index;                    // cell index for character or glyph
    boolean repaint;              // true if we should repaint our display
    String text;                  // mouse caption for this cell, if any

    /* Fetch the correct caption for the cell pointed to by the mouse. */

    index = convertMouse(event);  // convert pixel coordinates to index value
    repaint = false;              // assume that we won't need to repaint
    text = null;                  // assume no changes to caption string
    if (index < 0)                // is mouse is over defined character?
    {
      repaint |= (hoverIndex >= 0); // no, repaint if position has changed
      hoverIndex = NO_MOUSE;      // this character is no longer highlighted
      text = CharMap4.EMPTY_STATUS; // remove the caption string, if any
    }
    else if (hoverIndex != index) // has there been a change in position?
    {
      buffer = new StringBuffer(); // allocate empty string buffer for result
      ch = cellChar[index];       // character number or -1 if unmapped glyph
      hoverIndex = index;         // turn on highlighting for this character
      repaint = true;             // mark ourselves as needing to be repainted

      /* When displaying glyphs, always show the glyph number.  Then try to add
      information for a corresponding character number. */

      if (CharMap4.glyphFlag)     // are we displaying raw glyphs?
      {
        buffer.append("Glyph ");
        buffer.append(CharMap4.formatComma.format(cellGlyph[index]));
        buffer.append(" = ");
        if (ch < 0)
          buffer.append("no Unicode character mapping");
      }

      /* Java maps old 8-bit non-Unicode dingbat (symbol) fonts to the range
      between 0xF020 to 0xF0FF.  Since this is in the "private use" area of
      Unicode, we don't have a meaningful caption anyway unless we assume a
      remapping. */

      if ((ch >= 0xF020) && (ch <= 0xF0FF) && (charCount <= 256))
      {
        buffer.append(CharMap4.unicodeNotation(ch));
        buffer.append(" =? ");
        if (ch < 0xF07F)          // remapping to standard keyboard?
        {
          ch -= 0xF000;           // continue by assuming this character
        }
        else if (CharMap4.mswinFlag) // are we running on Microsoft Windows?
        {
          buffer.append(CharMap4.unicodeNotation(ch - 0xF000));
          buffer.append(" = ");
          buffer.append(CharMap4.winaltNotation(ch - 0xF000));
          ch = -1;                // that's the end of the caption
        }
        else                      // not keyboard map, not running Windows
        {
          buffer.append(CharMap4.unicodeNotation(ch - 0xF000));
          buffer.append(" = ");
          buffer.append("decimal ");
          buffer.append(CharMap4.formatComma.format(ch - 0xF000));
          ch = -1;                // that's the end of the caption
        }
      }

      /* Get a standard caption string if we still have a character number. */

      if (ch >= 0)                // do we have a character number?
      {
        buffer.append(CharMap4.captionGet(ch)); // get the standard caption
      }
      text = buffer.toString();   // convert string buffer to regular string
    }

    /* Don't waste time setting the dialog box if nothing has changed. */

    if ((text != null)            // if we constructed a new caption string
      && (CharMap4.statusDialog.getText().equals(text) == false))
    {
      CharMap4.statusDialog.setText(text); // copy new string to mouse caption
    }

    /* Avoid redrawing the screen unless the mouse has changed cells. */

    if ((clickIndex >= 0) && (clickIndex != index)) // away from old click?
    {
      clickIndex = NO_MOUSE;      // cancel forgotten highlight character
      repaint = true;             // mark ourselves as needing to be repainted
    }

    if (repaint) this.repaint();  // repaint our display if something changed

  } // end of mouseMoved() method

  public void mousePressed(MouseEvent event)
  {
    /* Called when a mouse button is first pressed, and before it is released.
    We highlight the cell that the mouse is pointing at. */

    int index;                    // cell index for character or glyph
    boolean repaint;              // true if we should repaint our display

    clickStartX = clickStartY = NO_MOUSE; // no starting coordinates for click
    index = convertMouse(event);  // convert pixel coordinates to index value
    repaint = false;              // assume that we won't need to repaint
    if (index >= 0)               // only if mouse is over defined character
    {
      clickIndex = hoverIndex = index; // turn on highlighting this character
      clickStartX = event.getX(); // get starting X coordinate of mouse click
      clickStartY = event.getY(); // get starting Y coordinate of mouse click
      repaint = true;             // mark ourselves as needing to be repainted
    }
    else if ((clickIndex >= 0) || (hoverIndex >= 0)) // previous highlights?
    {
      clickIndex = hoverIndex = NO_MOUSE; // cancel forgotten highlight char
      repaint = true;             // mark ourselves as needing to be repainted
    }

    if (repaint) this.repaint();  // repaint our display if something changed

  } // end of mousePressed() method

  public void mouseReleased(MouseEvent event)
  {
    /* Called after a mouse button is released, and before mouseClicked().  If
    the mouse moves too much, then Java doesn't call mouseClicked(), so we use
    this method to implement our own rules for how much a mouse can move while
    being clicked. */

    int index;                    // cell index for character or glyph
    boolean repaint;              // true if we should repaint our display

    index = convertMouse(event);  // convert pixel coordinates to index value
    repaint = false;              // assume that we won't need to repaint
    if ((index >= 0)              // only if mouse is over defined character
      && (clickIndex == index)    // and it's the same as when mouse pressed
      && (Math.abs(clickStartX - event.getX()) <= MOUSE_DRIFT)
      && (Math.abs(clickStartY - event.getY()) <= MOUSE_DRIFT))
    {
      /* Mouse is over a defined character or glyph. */

      if ((event.getButton() != MouseEvent.BUTTON1) // right means not primary
        || event.isAltDown() || event.isControlDown() || event.isShiftDown())
      {
        /* A right click or alternate key click invokes a pop-up menu with
        options to copy character or glyph numbers, captions, etc. */

        CharMap4.rightSaveCaption = CharMap4.statusDialog.getText(); // caption
        CharMap4.rightSaveChar = cellChar[index]; // save character number
        CharMap4.rightSaveGlyph = cellGlyph[index]; // save glyph number

        CharMap4.rightCopyCaption.setEnabled((CharMap4.rightSaveCaption != null)
          && (CharMap4.rightSaveCaption.length() > 0));
        CharMap4.rightCopyGlyph.setEnabled(CharMap4.rightSaveGlyph >= 0);
        CharMap4.rightCopyNotation.setEnabled(CharMap4.rightSaveChar >= 0);
        CharMap4.rightCopyNumber.setEnabled(CharMap4.rightSaveChar >= 0);
        CharMap4.rightCopyText.setEnabled(CharMap4.rightSaveChar >= 0);

        CharMap4.rightPopup.show(this, event.getX(), event.getY());
      }
      else
      {
        /* A left click or primary button click copies the character as text,
        if there is a unique character number. */

        if (cellChar[index] >= 0)
          mouseReplaceText(CharMap4.charToString(cellChar[index]));
      }
    }

    /* Avoid redrawing the screen unless the mouse has changed cells. */

    if (clickIndex >= 0)          // mouse release always ends click highlight
    {
      clickIndex = NO_MOUSE;      // this character is no longer highlighted
      repaint = true;             // mark ourselves as needing to be repainted
    }
    clickStartX = clickStartY = NO_MOUSE; // no starting coordinates for click

    if (hoverIndex != index)      // has there been a change in position?
    {
      hoverIndex = index;         // turn on highlighting for this character
      repaint = true;             // mark ourselves as needing to be repainted
    }

    if (repaint) this.repaint();  // repaint our display if something changed

  } // end of mouseReleased() method


/*
  mouseReplaceText() method

  After a mouse click, or the pop-up menu simulating a mouse click, call this
  method with a string to be added to the sample text.
*/
  void mouseReplaceText(String text)
  {
    if (CharMap4.clickReplace)    // does each click replace sample text?
      CharMap4.sampleDialog.setText(text);
    else                          // insert character or replace selection
      CharMap4.sampleDialog.replaceSelection(text);
    CharMap4.copyText();          // copy all sample text to clipboard
  }


/*
  mouseWheelMoved() method

  This is the mouse wheel listener, for the scroll wheel on some mice.  The
  "unit" scroll uses the local system preferences for how many lines/rows per
  click of the mouse.  The "unit" scroll may be too big if there are only one
  or two lines in the display.

  The mouse wheel listener has no interaction with the other mouse listeners
  above.
*/
  public void mouseWheelMoved(MouseWheelEvent event)
  {
    switch (event.getScrollType()) // different mice scroll differently
    {
      case (MouseWheelEvent.WHEEL_BLOCK_SCROLL):
        CharMap4.gridScroll.setValue(CharMap4.gridScroll.getValue()
          + (event.getWheelRotation()
          * CharMap4.gridScroll.getBlockIncrement()));
        break;

      case (MouseWheelEvent.WHEEL_UNIT_SCROLL):
        int i = CharMap4.gridScroll.getBlockIncrement(); // maximum scroll rows
        i = Math.max((-i), Math.min(i, event.getUnitsToScroll())); // limits
        CharMap4.gridScroll.setValue(CharMap4.gridScroll.getValue() + i);
                                  // scroll using limited local preferences
        break;

      default:                    // ignore anything that we don't recognize
        break;
    }
  } // end of mouseWheelMoved() method


/*
  paintComponent() method

  This is the "paint" method for a Java Swing component.  We have to worry
  about the window size changing, new options chosen by the user, etc.  There
  are many temporary variables in this method, because some calculations are
  difficult and declaring all variables at the beginning would be worse than
  declaring them when they are first used.
*/
  protected void paintComponent(Graphics context)
  {
    Graphics2D gr2d;              // special subclass of graphics context
    int i, k;                     // index variables
    FontRenderContext render;     // needed for displaying low-level glyphs

    /* Most of this code would work with the standard Graphics object, but some
    of the glyph routines need the newer Graphics2D subclass. */

    gr2d = (Graphics2D) context;  // another name for the same graphics context
    render = gr2d.getFontRenderContext(); // for displaying low-level glyphs

    /* Erase the entire panel using our choice of background colors. */

    gr2d.setColor(PANEL_COLOR);   // flood fill with background color
    gr2d.fillRect(0, 0, this.getWidth(), this.getHeight());

    /* If the font has changed, then we need to redo both the height and the
    width, and we must collect new information about the font. */

    boolean redoHeight = false;   // assume that panel height doesn't change
    boolean redoWidth = false;    // assume that panel width doesn't change

    if (CharMap4.displayFont == null) // is there a font to display characters?
      return;                     // no, then can't do anything more
    else if (CharMap4.displayFont.equals(panelFont) == false) // a new font?
    {
      clickIndex = hoverIndex = NO_MOUSE; // cancel forgotten highlight char
      cornerIndex = 0;            // force top-left corner to first character
      panelFont = CharMap4.displayFont; // save current character display font
      redoHeight = redoWidth = true; // force both directions to be redone

      /* Get the font metrics.  We want the "official" maximum height and
      width.  Note that even though a font provides this information, there may
      still be characters that draw outside the declared bounds.  For critical
      applications, you would have to call FontMetrics.getMaxCharBounds(), and
      that may be slow if the font is very large (not tested). */

      fontData = gr2d.getFontMetrics(panelFont); // save a copy of metrics
      lineAscent = fontData.getAscent(); // number of pixels above baseline
      lineHeight = fontData.getHeight(); // height of each line in pixels
      maxWidth = Math.max(10, fontData.getMaxAdvance()); // maximum all chars
      horizStep = maxWidth + (2 * TEXT_MARGIN) + GRID_WIDTH; // between cells
      vertiStep = lineHeight + (2 * TEXT_MARGIN) + GRID_WIDTH; // between cells

      /* The <charTemp> array is indexed by Unicode character number and has a
      non-negative glyph number for each character, or -1 for no mapping.  We
      only need the first non-spacing glyph even if a character maps to more
      than one glyph. */

      charCount = 0;              // start with no characters in the list
      int[] charTemp = new int[CharMap4.MAX_UNICODE + 1]; // use maximum size
      for (i = 0; i < charTemp.length; i ++)
        charTemp[i] = -1;         // default to no mapping for all characters

      /* The <glyphChar> array is indexed by internal glyph number and has a
      non-negative character number for each glyph, or -1 for no mapping.  We
      save only the first character even if a glyph is used by more than one
      character.  It would be nice to have a complete list of characters that
      map to each glyph, but with upwards of 50,000 glyphs in some fonts, this
      would take too many resources (too much memory for Vector objects and too
      much processing time). */

      glyphCount = Math.max(0, panelFont.getNumGlyphs()); // don't trust source
      int glymissing = panelFont.getMissingGlyphCode(); // undefined characters
      glyphChar = new int[glyphCount]; // we always know final size for these
      glyphGlyph = new int[glyphCount];
      for (i = 0; i < glyphCount; i ++)
      {
        glyphChar[i] = -1;        // default to no mapping for all glyphs
        glyphGlyph[i] = i;        // assume that all glyphs map to themselves!
      }

      /* Enumerate all possible Unicode characters. */

      for (i = CharMap4.MIN_UNICODE; i <= CharMap4.MAX_UNICODE; i ++)
      {
        /* Ignore characters that Java knows it can't display. */

//      if (panelFont.canDisplay((char) i) == false) // Java 1.4
        if (panelFont.canDisplay(i) == false) // Java 5.0
          continue;               // jump to next interation of <for> loop

        /* Update mapping information between characters and glyphs.  Early
        Java 5.0 on the Apple Macintosh has a bug where canDisplay() returns
        true for every possible Unicode character number.  Ignore characters
        that map to the "missing" glyph (usually number 0) or to a "spacing"
        glyph (often the out-of-range glyph number of 65,535). */

        GlyphVector glyvector = panelFont.createGlyphVector(render, CharMap4
          .charToString(i));      // get glyph list for this character
        int glycount = glyvector.getNumGlyphs(); // supposed number of glyphs
        for (k = 0; k < glycount; k ++) // for each glyph in the glyph vector
        {
          int glyph = glyvector.getGlyphCode(k); // get one glyph number
          if ((glyph >= 0) && (glyph < glyphCount) && (glyph != glymissing))
          {                       // ignore missing and spacing glyphs
            if (charTemp[i] < 0)  // does this character already have a glyph?
              charTemp[i] = glyph; // no, save the first good glyph we find
            if (glyphChar[glyph] < 0) // does this glyph already have a char?
              glyphChar[glyph] = i; // no, save the first character we find
          }
        }
        if (charTemp[i] >= 0)     // count each character as displayable ...
          charCount ++;           // ... only if a non-spacing glyph found
      }

      /* Use <charTemp> to create compressed (smaller) arrays for only those
      characters that can be displayed. */

      charChar = new int[charCount]; // list of Unicode character numbers
      charGlyph = new int[charCount]; // list of internal glyph numbers
      k = 0;                      // place displayable entries starting here
      for (i = CharMap4.MIN_UNICODE; i <= CharMap4.MAX_UNICODE; i ++)
      {
        int glyph = charTemp[i];  // get glyph number, if any
        if (glyph >= 0)           // if character mapped to at least one glyph
        {
          charChar[k] = i;        // save Unicode character number
          charGlyph[k] = glyph;   // save glyph number, if any
          k ++;                   // finish one more displayable character
        }
      }
      charTemp = null;            // release memory used by this larger array

      /* Protect ourselves from fonts that have no displayable characters. */

      CharMap4.statusDialog.setText(CharMap4.formatComma.format(charCount)
        + " characters with " + CharMap4.formatComma.format(glyphCount)
        + " glyphs");             // subvert "mouse caption" for extra trivia

      if ((charCount <= 0) || (glyphCount <= 0)) // need at least one defined
      {
        panelFont = null;         // crude, but prevents font from being used
        return;                   // give up, again and again, on each call
      }
    }

    /* Set up our display cells using either character or glyph data. */

    if (CharMap4.glyphFlag)       // are we displaying raw glyphs?
    {
      cellChar = glyphChar;
      cellCount = glyphCount;
      cellGlyph = glyphGlyph;
    }
    else                          // no, doing Unicode characters
    {
      cellChar = charChar;
      cellCount = charCount;
      cellGlyph = charGlyph;
    }

    /* If the panel width has changed, then we need to recalculate how many
    complete columns of text can be displayed inside this panel with the
    specified margins.  We don't want partial columns, because there is no
    horizontal scroll bar, only a vertical scroll bar. */

    if (redoWidth || (this.getWidth() != panelWidth))
    {
      panelWidth = this.getWidth(); // save current panel width in pixels
      redoWidth = true;           // remember that the width has changed

      panelColumns = Math.max(1,
        ((panelWidth - (2 * PANEL_MARGIN) - GRID_WIDTH) / horizStep));
    }

    /* If the panel height has changed, then we need to recalculate how many
    complete lines (rows) can be displayed.  The scroll bar handles any partial
    later lines that aren't included in our row count. */

    if (redoHeight || (this.getHeight() != panelHeight))
    {
      panelHeight = this.getHeight(); // save current panel height in pixels
      redoHeight = true;          // remember that the height has changed

      panelRows = Math.max(1,
        ((panelHeight - (2 * PANEL_MARGIN) - GRID_WIDTH) / vertiStep));
    }

    /* When the window size changes, we need to recalculate several settings
    for the vertical scroll bar.  These are otherwise static except the current
    position.  As a programming note, please call setValues() when setting more
    than one of the parameters, otherwise the change listener may fire between
    calls to the individual methods for setting parameters.  The maximum value
    is one more than what you might expect, but that's what Java wants. */

    if ((cellCount != panelCount) || redoHeight || redoWidth) // changed size?
    {
      panelCount = cellCount;     // save current number of displayed cells
      int row = cornerIndex / panelColumns;
                                  // convert character index to row number
      row = Math.max(0, Math.min(row, ((cellCount / panelColumns)
        - panelRows + 1)));       // if possible, don't leave blank rows at end
      CharMap4.gridScroll.setValues(row, // scroll value
        panelRows,                // extent (visible amount)
        0,                        // minimum: always zero
        ((cellCount + panelColumns - 1) / panelColumns));
                                  // maximum: allow partial last row
      cornerIndex = CharMap4.gridScroll.getValue() * panelColumns;
                                  // convert scroll row back to character index

      CharMap4.gridScroll.setBlockIncrement(Math.max(1, (panelRows - 1)));
                                  // lines/rows per "scroll one page"
      CharMap4.gridScroll.setUnitIncrement(1); // rows per "scroll one line"
    }

    /* Draw each character that is wholely or partially visible in the current
    grid.  The code below is very sloppy: it calls a subroutine for all index
    values that *might* be defined, including a partial next line. */

    int end = cornerIndex + (panelColumns * (panelRows + 1)) - 1; // partials
    for (i = cornerIndex; i <= end; i ++) // display all possible characters
      paintGridCell(gr2d, render, i); // paint each and every possible cell

  } // end of paintComponent() method


/*
  paintGridCell() method

  The caller wants us to paint one cell in the character grid.  The cell may or
  may not be defined.  The cell may be highlighted during mouse clicks.

  Please note that many fonts draw outside of their declared bounding boxes!
  View a font like "Lucida Console" before making any judgements about the
  accuracy of this method.
*/
  void paintGridCell(Graphics2D gr2d, FontRenderContext render, int index)
  {
    if (index >= cellCount)       // is there a defined character?
      return;                     // no, do nothing and return to caller

    /* Calculate top-left drawing corner of the border for this cell. */

    int x = (((index - cornerIndex) % panelColumns) * horizStep) + PANEL_MARGIN;
    int y = (((index - cornerIndex) / panelColumns) * vertiStep) + PANEL_MARGIN;

    /* Despite appearances, we don't actually draw lines for the grid!  We
    erase a rectangle with the gridline color, then later a smaller interior
    rectangle with the background color. */

    gr2d.setColor(GRID_COLOR);    // flood fill with gridline color
    gr2d.fillRect(x, y, (horizStep + GRID_WIDTH), (vertiStep + GRID_WIDTH));

    /* If we're not highlighting, then clear the interior of the cell to the
    background color. */

    if (index == clickIndex)      // is the mouse clicking on this character?
      gr2d.setColor(TEXT_COLOR);  // click reverses background-foreground
    else if (index == hoverIndex) // is the mouse over this character?
      { /* do nothing: keep gridline color */ }
    else
      gr2d.setColor(PANEL_COLOR); // mouse is elsewhere, normal background
    gr2d.fillRect((x + GRID_WIDTH), (y + GRID_WIDTH),
      (horizStep - GRID_WIDTH), (vertiStep - GRID_WIDTH));

    /* Draw the defined character or raw glyph. */

    gr2d.setColor((index == clickIndex) ? PANEL_COLOR : TEXT_COLOR);
    if (CharMap4.glyphFlag)       // are we displaying raw glyphs?
    {
      int[] list = new int[1];    // need a list for createGlyphVector()
      list[0] = cellGlyph[index]; // only value in list is the glyph index
      GlyphVector glyvector = panelFont.createGlyphVector(render, list);
      gr2d.drawGlyphVector(glyvector,
        (x + GRID_WIDTH + TEXT_MARGIN + ((maxWidth
          - ((int) glyvector.getGlyphMetrics(0).getAdvanceX())) / 2)),
        (y + GRID_WIDTH + TEXT_MARGIN + lineAscent));
    }
    else                          // no, displaying standard characters
    {
      int ch = cellChar[index];   // get the character we want to display
      gr2d.setFont(panelFont);    // set the correct font
      gr2d.drawString(CharMap4.charToString(ch),
        (x + GRID_WIDTH + TEXT_MARGIN + ((maxWidth
          - fontData.charWidth(ch)) / 2)),
        (y + GRID_WIDTH + TEXT_MARGIN + lineAscent));
    }
  } // end of paintGridCell() method


/*
  stateChanged() method

  Currently only used for the vertical scroll bar.  This method gets called
  often, perhaps too often.  Try to invoke other methods only if something
  important has changed.
*/
  public void stateChanged(ChangeEvent event)
  {
    if (panelFont != null)        // are we ready to handle this yet?
    {
      int scroll = CharMap4.gridScroll.getValue(); // scroll bar row position
      int newCorner = scroll * panelColumns; // convert rows to characters
      if (newCorner != cornerIndex) // has drawing position truly changed?
      {
        cornerIndex = newCorner;  // yes, remember new starting position
        this.repaint();           // mark ourselves as needing to be repainted
      }
    }
  } // end of stateChanged() method

} // end of CharMap4Grid class

// ------------------------------------------------------------------------- //

/*
  CharMap4User class

  This class listens to input from the user and passes back event parameters to
  a static method in the main class.
*/

class CharMap4User extends AbstractAction implements Runnable
{
  /* constructor */

  public CharMap4User(String command)
  {
    super();                      // initialize our superclass (AbstractAction)
    this.putValue(Action.NAME, command); // save action name for later decoding
  }

  /* button listener, dialog boxes, keyboard, etc */

  public void actionPerformed(ActionEvent event)
  {
    String command = (String) this.getValue(Action.NAME); // get saved action
    if (command == null)          // was there a keyboard action name?
      CharMap4.userButton(event); // no, process as regular button or dialog
    else                          // yes, there was a saved keyboard action
      CharMap4.userKey(command);  // process as a regular keyboard command
  }

  /* separate heavy-duty processing thread */

  public void run()
  {
    CharMap4.loadConfig();        // load all possible mouse caption strings
  }

} // end of CharMap4User class

/* Copyright (c) 2008 by Keith Fenske.  Apache License or GNU GPL. */
