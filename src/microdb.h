
#ifndef MICRODB_INC_H_
#define MICRODB_INC_H_

#define META_KEY "meta"
#define KEY_INSTANCEID "instance"
#define KEY_INDICIES "indicies"
#define KEY_ID "id"

#include <microdb/value.h>
#include <microdb/serialize.h>
#include <microdb/status.h>
#include <microdb/microdb.h>

#include "log.h"
#include "utils.h"
#include "sha256.h"
#include "uuid.h"
#include "driver.h"
#include "viewquery.h"
#include "index.h"
#include "dbfunctions.h"


#endif // MICRODB_INC_H_