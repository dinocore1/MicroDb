
%option noyywrap
%option reentrant
%option prefix="uuid"
%option extra-type="microdb::parse_t*"

%{

#include <string>
#include "uuid.h"

namespace microdb {

  typedef struct parse_t {
    uint8_t* mData;
    bool mSuccess;

    parse_t(uint8_t* data)
    : mData(data), mSuccess(false) {}

  } parse_t;

}

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

{DIGIT}{8}"-"{DIGIT}{4}"-"{DIGIT}{4}"-"{DIGIT}{4}"-"{DIGIT}{12} {
    yyextra->mData[0] = hexPairToChar(yytext[0], yytext[1]);
    yyextra->mData[1] = hexPairToChar(yytext[2], yytext[3]);
    yyextra->mData[2] = hexPairToChar(yytext[4], yytext[5]);
    yyextra->mData[3] = hexPairToChar(yytext[6], yytext[7]);

    yyextra->mData[4] = hexPairToChar(yytext[9], yytext[10]);
    yyextra->mData[5] = hexPairToChar(yytext[11], yytext[12]);

    yyextra->mData[6] = hexPairToChar(yytext[14], yytext[15]);
    yyextra->mData[7] = hexPairToChar(yytext[16], yytext[17]);

    yyextra->mData[8] = hexPairToChar(yytext[19], yytext[20]);
    yyextra->mData[9] = hexPairToChar(yytext[21], yytext[22]);

    yyextra->mData[10] = hexPairToChar(yytext[24], yytext[25]);
    yyextra->mData[11] = hexPairToChar(yytext[26], yytext[27]);
    yyextra->mData[12] = hexPairToChar(yytext[28], yytext[29]);
    yyextra->mData[13] = hexPairToChar(yytext[30], yytext[31]);
    yyextra->mData[14] = hexPairToChar(yytext[32], yytext[33]);
    yyextra->mData[15] = hexPairToChar(yytext[34], yytext[35]);

    yyextra->mSuccess = true;
}

{DIGIT}{32} {
    yyextra->mData[0] = hexPairToChar(yytext[0], yytext[1]);
    yyextra->mData[1] = hexPairToChar(yytext[2], yytext[3]);
    yyextra->mData[2] = hexPairToChar(yytext[4], yytext[5]);
    yyextra->mData[3] = hexPairToChar(yytext[6], yytext[7]);
    yyextra->mData[4] = hexPairToChar(yytext[8], yytext[9]);
    yyextra->mData[5] = hexPairToChar(yytext[10], yytext[11]);
    yyextra->mData[6] = hexPairToChar(yytext[12], yytext[13]);
    yyextra->mData[7] = hexPairToChar(yytext[14], yytext[15]);
    yyextra->mData[8] = hexPairToChar(yytext[16], yytext[17]);
    yyextra->mData[9] = hexPairToChar(yytext[18], yytext[19]);
    yyextra->mData[10] = hexPairToChar(yytext[20], yytext[21]);
    yyextra->mData[11] = hexPairToChar(yytext[22], yytext[23]);
    yyextra->mData[12] = hexPairToChar(yytext[24], yytext[25]);
    yyextra->mData[13] = hexPairToChar(yytext[26], yytext[27]);
    yyextra->mData[14] = hexPairToChar(yytext[28], yytext[29]);
    yyextra->mData[15] = hexPairToChar(yytext[30], yytext[31]);

    yyextra->mSuccess = true;
}

. ;

%%

namespace microdb {

    bool UUID::parse(const char* value) {

        parse_t ctx(mData);

        yyscan_t scanner;
        uuidlex_init(&scanner);
        uuidlex_init_extra(&ctx, &scanner);

        YY_BUFFER_STATE buff = uuid_scan_string(value, scanner);
        uuidlex(scanner);

        uuid_delete_buffer(buff, scanner);
        uuidlex_destroy(scanner);

        return ctx.mSuccess;
    }
}
