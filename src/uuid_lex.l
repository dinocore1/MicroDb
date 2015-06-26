
%option noyywrap
%option reentrant
%option prefix="uuid"
%option extra-type="uint8_t*"

%{

#include <string>
#include "uuid.h"


// converts a single hex char to a number (0 - 15)
unsigned char hexDigitToChar(char ch) {
    if (ch > 47 && ch < 58)
    return ch - 48;
    
    if (ch > 96 && ch < 103)
    return ch - 87;
    
    if (ch > 64 && ch < 71)
    return ch - 55;
    
    return 0;
}

// converts the two hexadecimal characters to an unsigned char (a byte)
unsigned char hexPairToChar(char a, char b) {
    return hexDigitToChar(a) * 16 + hexDigitToChar(b);
}

%}

DIGIT       [0-9a-fA-F]

%%

{DIGIT} { printf("found digit"); }

{DIGIT}{8}"-"{DIGIT}{4}"-"{DIGIT}{4}"-"{DIGIT}{4}"-"{DIGIT}{12} {
    yyextra[0] = hexPairToChar(yytext[0], yytext[1]);
    yyextra[1] = hexPairToChar(yytext[2], yytext[3]);
    yyextra[2] = hexPairToChar(yytext[4], yytext[5]);
    yyextra[3] = hexPairToChar(yytext[6], yytext[7]);
    
    yyextra[4] = hexPairToChar(yytext[9], yytext[10]);
    yyextra[5] = hexPairToChar(yytext[11], yytext[12]);
    
    yyextra[6] = hexPairToChar(yytext[14], yytext[15]);
    yyextra[7] = hexPairToChar(yytext[16], yytext[17]);
    
    yyextra[8] = hexPairToChar(yytext[19], yytext[20]);
    yyextra[9] = hexPairToChar(yytext[21], yytext[22]);
    
    yyextra[10] = hexPairToChar(yytext[24], yytext[25]);
    yyextra[11] = hexPairToChar(yytext[26], yytext[27]);
    yyextra[12] = hexPairToChar(yytext[28], yytext[29]);
    yyextra[13] = hexPairToChar(yytext[30], yytext[31]);
    yyextra[14] = hexPairToChar(yytext[32], yytext[33]);
    yyextra[15] = hexPairToChar(yytext[34], yytext[35]);
}

%%

namespace microdb {
    
    UUID::UUID(const char* value) {
        yyscan_t scanner;
        uuidlex_init(&scanner);
        uuidlex_init_extra(mData, &scanner);
        
        YY_BUFFER_STATE buff = uuid_scan_string(value, scanner);
        uuidlex(scanner);
        
        uuid_delete_buffer(buff, scanner);
        
        uuidlex_destroy(scanner);
    }
}