// $ANTLR 3.2 Sep 23, 2009 12:02:23 Company.g 2011-04-26 14:35:30

package org.softlang.antlr;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class Company extends Lexer {
    public static final int OPEN=10;
    public static final int COMPANY=4;
    public static final int WS=12;
    public static final int CLOSE=11;
    public static final int ADDRESS=8;
    public static final int SALARY=9;
    public static final int EMPLOYEE=6;
    public static final int MANAGER=7;
    public static final int FLOAT=15;
    public static final int DIGIT=14;
    public static final int EOF=-1;
    public static final int DEPARTMENT=5;
    public static final int STRING=13;

      @Override
      public void reportError(RecognitionException e) {
        throw new IllegalArgumentException(e);
      }


    // delegates
    // delegators

    public Company() {;} 
    public Company(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public Company(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "Company.g"; }

    // $ANTLR start "COMPANY"
    public final void mCOMPANY() throws RecognitionException {
        try {
            int _type = COMPANY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Company.g:18:13: ( 'company' )
            // Company.g:18:15: 'company'
            {
            match("company"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPANY"

    // $ANTLR start "DEPARTMENT"
    public final void mDEPARTMENT() throws RecognitionException {
        try {
            int _type = DEPARTMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Company.g:19:13: ( 'department' )
            // Company.g:19:15: 'department'
            {
            match("department"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DEPARTMENT"

    // $ANTLR start "EMPLOYEE"
    public final void mEMPLOYEE() throws RecognitionException {
        try {
            int _type = EMPLOYEE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Company.g:20:13: ( 'employee' )
            // Company.g:20:15: 'employee'
            {
            match("employee"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EMPLOYEE"

    // $ANTLR start "MANAGER"
    public final void mMANAGER() throws RecognitionException {
        try {
            int _type = MANAGER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Company.g:21:13: ( 'manager' )
            // Company.g:21:15: 'manager'
            {
            match("manager"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MANAGER"

    // $ANTLR start "ADDRESS"
    public final void mADDRESS() throws RecognitionException {
        try {
            int _type = ADDRESS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Company.g:22:13: ( 'address' )
            // Company.g:22:15: 'address'
            {
            match("address"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ADDRESS"

    // $ANTLR start "SALARY"
    public final void mSALARY() throws RecognitionException {
        try {
            int _type = SALARY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Company.g:23:13: ( 'salary' )
            // Company.g:23:15: 'salary'
            {
            match("salary"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SALARY"

    // $ANTLR start "OPEN"
    public final void mOPEN() throws RecognitionException {
        try {
            int _type = OPEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Company.g:24:13: ( '{' )
            // Company.g:24:15: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OPEN"

    // $ANTLR start "CLOSE"
    public final void mCLOSE() throws RecognitionException {
        try {
            int _type = CLOSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Company.g:25:13: ( '}' )
            // Company.g:25:15: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CLOSE"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Company.g:26:13: ( ( ' ' | ( '\\r' )? '\\n' | '\\t' )+ )
            // Company.g:26:17: ( ' ' | ( '\\r' )? '\\n' | '\\t' )+
            {
            // Company.g:26:17: ( ' ' | ( '\\r' )? '\\n' | '\\t' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=4;
                switch ( input.LA(1) ) {
                case ' ':
                    {
                    alt2=1;
                    }
                    break;
                case '\n':
                case '\r':
                    {
                    alt2=2;
                    }
                    break;
                case '\t':
                    {
                    alt2=3;
                    }
                    break;

                }

                switch (alt2) {
            	case 1 :
            	    // Company.g:26:18: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 2 :
            	    // Company.g:26:22: ( '\\r' )? '\\n'
            	    {
            	    // Company.g:26:22: ( '\\r' )?
            	    int alt1=2;
            	    int LA1_0 = input.LA(1);

            	    if ( (LA1_0=='\r') ) {
            	        alt1=1;
            	    }
            	    switch (alt1) {
            	        case 1 :
            	            // Company.g:26:22: '\\r'
            	            {
            	            match('\r'); 

            	            }
            	            break;

            	    }

            	    match('\n'); 

            	    }
            	    break;
            	case 3 :
            	    // Company.g:26:33: '\\t'
            	    {
            	    match('\t'); 

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Company.g:27:13: ( '\"' (~ '\"' )* '\"' )
            // Company.g:27:17: '\"' (~ '\"' )* '\"'
            {
            match('\"'); 
            // Company.g:27:21: (~ '\"' )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='\u0000' && LA3_0<='!')||(LA3_0>='#' && LA3_0<='\uFFFF')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // Company.g:27:22: ~ '\"'
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "FLOAT"
    public final void mFLOAT() throws RecognitionException {
        try {
            int _type = FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Company.g:28:13: ( ( DIGIT )+ ( '.' ( DIGIT )+ )? )
            // Company.g:28:15: ( DIGIT )+ ( '.' ( DIGIT )+ )?
            {
            // Company.g:28:15: ( DIGIT )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='0' && LA4_0<='9')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // Company.g:28:15: DIGIT
            	    {
            	    mDIGIT(); 

            	    }
            	    break;

            	default :
            	    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);

            // Company.g:28:22: ( '.' ( DIGIT )+ )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='.') ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // Company.g:28:23: '.' ( DIGIT )+
                    {
                    match('.'); 
                    // Company.g:28:27: ( DIGIT )+
                    int cnt5=0;
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( ((LA5_0>='0' && LA5_0<='9')) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // Company.g:28:27: DIGIT
                    	    {
                    	    mDIGIT(); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt5 >= 1 ) break loop5;
                                EarlyExitException eee =
                                    new EarlyExitException(5, input);
                                throw eee;
                        }
                        cnt5++;
                    } while (true);


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FLOAT"

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // Company.g:30:16: ( ( '0' .. '9' ) )
            // Company.g:30:18: ( '0' .. '9' )
            {
            // Company.g:30:18: ( '0' .. '9' )
            // Company.g:30:19: '0' .. '9'
            {
            matchRange('0','9'); 

            }


            }

        }
        finally {
        }
    }
    // $ANTLR end "DIGIT"

    public void mTokens() throws RecognitionException {
        // Company.g:1:8: ( COMPANY | DEPARTMENT | EMPLOYEE | MANAGER | ADDRESS | SALARY | OPEN | CLOSE | WS | STRING | FLOAT )
        int alt7=11;
        alt7 = dfa7.predict(input);
        switch (alt7) {
            case 1 :
                // Company.g:1:10: COMPANY
                {
                mCOMPANY(); 

                }
                break;
            case 2 :
                // Company.g:1:18: DEPARTMENT
                {
                mDEPARTMENT(); 

                }
                break;
            case 3 :
                // Company.g:1:29: EMPLOYEE
                {
                mEMPLOYEE(); 

                }
                break;
            case 4 :
                // Company.g:1:38: MANAGER
                {
                mMANAGER(); 

                }
                break;
            case 5 :
                // Company.g:1:46: ADDRESS
                {
                mADDRESS(); 

                }
                break;
            case 6 :
                // Company.g:1:54: SALARY
                {
                mSALARY(); 

                }
                break;
            case 7 :
                // Company.g:1:61: OPEN
                {
                mOPEN(); 

                }
                break;
            case 8 :
                // Company.g:1:66: CLOSE
                {
                mCLOSE(); 

                }
                break;
            case 9 :
                // Company.g:1:72: WS
                {
                mWS(); 

                }
                break;
            case 10 :
                // Company.g:1:75: STRING
                {
                mSTRING(); 

                }
                break;
            case 11 :
                // Company.g:1:82: FLOAT
                {
                mFLOAT(); 

                }
                break;

        }

    }


    protected DFA7 dfa7 = new DFA7(this);
    static final String DFA7_eotS =
        "\14\uffff";
    static final String DFA7_eofS =
        "\14\uffff";
    static final String DFA7_minS =
        "\1\11\13\uffff";
    static final String DFA7_maxS =
        "\1\175\13\uffff";
    static final String DFA7_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13";
    static final String DFA7_specialS =
        "\14\uffff}>";
    static final String[] DFA7_transitionS = {
            "\2\11\2\uffff\1\11\22\uffff\1\11\1\uffff\1\12\15\uffff\12\13"+
            "\47\uffff\1\5\1\uffff\1\1\1\2\1\3\7\uffff\1\4\5\uffff\1\6\7"+
            "\uffff\1\7\1\uffff\1\10",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
    static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
    static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
    static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
    static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
    static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
    static final short[][] DFA7_transition;

    static {
        int numStates = DFA7_transitionS.length;
        DFA7_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
        }
    }

    class DFA7 extends DFA {

        public DFA7(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 7;
            this.eot = DFA7_eot;
            this.eof = DFA7_eof;
            this.min = DFA7_min;
            this.max = DFA7_max;
            this.accept = DFA7_accept;
            this.special = DFA7_special;
            this.transition = DFA7_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( COMPANY | DEPARTMENT | EMPLOYEE | MANAGER | ADDRESS | SALARY | OPEN | CLOSE | WS | STRING | FLOAT );";
        }
    }
 

}