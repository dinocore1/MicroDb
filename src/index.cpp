
#include "microdb.h"
#include "index.h"

using namespace std;
using namespace std::placeholders;

namespace microdb {
	
	Value createPrimaryIndexEntry(const Value& primaryKey) {
		Value retval;
		
		retval.Add("o");
		retval.Add(primaryKey);
		
		return retval;
	}
	
	class PrimaryIndex : public Index {
		private:
		void setQuery(std::unique_ptr< ViewQuery >& ptr) = delete;
			
		public:
		PrimaryIndex()
		: Index("primary") { }
		
		void index(Value& obj, emitCallback cb) {
			Value key = obj.Get(KEY_ID);
			if(key.IsNull()) {
				key = UUID::createRandom();
				obj.Set(KEY_ID, key);
			}
			
			Value indexEntry = createPrimaryIndexEntry(key);
			cb(key, obj, indexEntry);
		}
		
	};
	
	PrimaryIndex mPrimaryIndex;
	
	Index& getPrimaryIndex() {
		return mPrimaryIndex;
	}
	
	Index::Index(const std::string& name)
	: mName(name) { }
	
	const std::string Index::getName() const {
		return mName;
	}
	
	void Index::setQuery(std::unique_ptr<ViewQuery> ptr) {
		mQuery = std::move( ptr );
	}
	
	void Index::emit(Value& retval, const std::vector< Selector* >& args, emitCallback cb) {
		
		if(cb) {
			Value key, value;
			
			const int argsSize = args.size();
			if(argsSize >= 1) {
				args[0]->select(this, key);
				if(argsSize == 2) {
					args[1]->select(this, value);
				}
				
				Value indexEntry;
				indexEntry.Add("i");
				indexEntry.Add(mName);
				indexEntry.Add(key);
				
				Value obj = GetVar("obj");
				indexEntry.Add(obj[KEY_ID]);
				
				cb(key, value, indexEntry);
			}
		}
	}
	
	void Index::index(Value& obj, emitCallback cb) {
		mVariables.clear();
		SetVar("obj", obj);
		SetFunction("emit", std::bind(&Index::emit, this, _2, _3, cb) );
		mQuery->execute(this);
	}
	
	
} // namespace microdb