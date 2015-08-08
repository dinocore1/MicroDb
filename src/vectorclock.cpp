
#include <string>
#include <unordered_map>
#include <set>
#include <microdb/value.h>
#include "vectorclock.h"

using namespace std;

namespace microdb {

  typedef std::unordered_map<std::string, uint64_t> ValueType;

  VectorClock::VectorClock() {

  }

  VectorClock::VectorClock(const Value& v) {
    for(const std::string& key : v.GetKeys()) {
      mIdMap[key] = v[key].asUint64();
    }
  }

  uint64_t getValue(const ValueType& map, const string& key) {
    auto entry = map.find(key);
    if(entry == map.end()) {
      return 0;
    } else {
      return entry->second;
    }
  }

  void VectorClock::increment(const string& key) {
    mIdMap[key] += 1;
  }

  bool VectorClock::isLessThan(const VectorClock& o) const {
    set<string> keys;
    for(auto entry : mIdMap) {
      keys.emplace(entry.first);
    }

    for(auto entry : o.mIdMap) {
      keys.emplace(entry.first);
    }

    bool isOneLessThan = false;
    for(const string& key : keys) {
      uint64_t a = getValue(mIdMap, key);
      uint64_t b = getValue(o.mIdMap, key);

      if(a < b) {
        isOneLessThan = true;
      } else if( a > b) {
        return false;
      }
    }

    return isOneLessThan;
  }

  bool VectorClock::operator< (const VectorClock& o) const {
    return isLessThan(o);
  }

  Value VectorClock::toValue() const {
    Value retval;
    for(auto entry : mIdMap) {
      retval[entry.first] = entry.second;
    }

    return retval;
  }


} // namespace microdb
