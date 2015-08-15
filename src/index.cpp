
#include "microdb.h"
#include "index.h"

using namespace std;

namespace microdb {
	
	class PrimaryIndex : public Index {
		private:
		void setQuery(std::unique_ptr< ViewQuery >& ptr) = delete;
			
		public:
		PrimaryIndex()
		: Index("primary") {
			SetFunction("setValue", setValue);
			SetFunction("createUUID", createUUID);
			
			unique_ptr< ViewQuery> query(new ViewQuery());
			query->compile("if(obj.id == null) { setValue(obj, \"id\", createUUID() } emit(obj.id)");
			mQuery = std::move(query);
		}
		
		
	};
	
	PrimaryIndex mPrimaryIndex;
	
	Index& getPrimaryIndex() {
		return mPrimaryIndex;
	}
	
	void indexEmit(Environment* env, Value& retval, const std::vector< Selector* >& args) {
		Index* index = reinterpret_cast<Index*>(env);
		Value& obj = env->GetVar("obj");
		index->emit(args, obj);
		
		//TODO mSequence++
	}
	
	Index::Index(const std::string& name)
	: mName(name) {
		SetFunction("emit", indexEmit);
	}
	
	
	
} // namespace microdb