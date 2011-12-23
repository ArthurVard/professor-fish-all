// Adopted from http://en.wikipedia.org/wiki/Quine_(computing)

package quine;

public class Wikipedia
{
  public static void main( String[] args )
  {
    char q = 34;      // Quotation mark character
    String[] l = {    // Array of source code
    "package quine;",
    "",
    "public class Wikipedia",
    "{",
    "  public static void main( String[] args )",
    "  {",
    "    char q = 34;      // Quotation mark character",
    "    String[] l = {    // Array of source code",
    "    ",
    "    };",
    "    for( int i = 0; i < 8; i++ )           // Print opening code",
    "        System.out.println( l[i] );",
    "    for( int i = 0; i < l.length; i++ )    // Print string array",
    "        System.out.println( l[8] + q + l[i] + q + ',' );",
    "    for( int i = 9; i < l.length; i++ )    // Print this code",
    "        System.out.println( l[i] );",
    "  }",
    "}",
    };
    for( int i = 0; i < 8; i++ )           // Print opening code
        System.out.println( l[i] );
    for( int i = 0; i < l.length; i++ )    // Print string array
        System.out.println( l[8] + q + l[i] + q + ',' );
    for( int i = 9; i < l.length; i++ )    // Print this code
        System.out.println( l[i] );
  }
}