
#include "microdb.h"
#include "index.h"

using namespace std;

namespace microdb {
	
	void createUUID(Environment* env, Value& retval, const std::vector< Selector* >& args) {
		UUID id = UUID::createRandom();
		retval = id.getString();
	}
	
	class PrimaryIndex : public Index {
		private:
		void setQuery(std::unique_ptr< ViewQuery >& ptr) = delete;
			
		public:
		PrimaryIndex()
		: Index("primary") {
			unique_ptr< ViewQuery> query(new ViewQuery());
			query->compile("if(obj.id == null) { setValue(obj, \"id\", createUUID() }");
			mQuery = std::move(query);
			
			SetFunction("createUUID", createUUID);
		}
		
		
	};
	
	PrimaryIndex mPrimaryIndex;
	
	Index& getPrimaryIndex() {
		return mPrimaryIndex;
	}
	
	Index::Index(const std::string& name)
	: mName(name) {
		
	}
	
	
	
} // namespace microdb