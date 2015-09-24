
#include "microdb.h"
#include "index.h"

using namespace std;
using namespace std::placeholders;

namespace microdb {
	
	Value Index::createPrimaryIndexEntry(const Value& primaryKey) {
		Value retval;
		
		retval.Add('i');
		retval.Add("primary");
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
			Value key;
			if(!obj.HasKey(KEY_ID) || !(key = obj[KEY_ID]).IsString()) {
				key = UUID::createRandom().getString();
				obj.Set(KEY_ID, key);
			}
			
			Value indexEntry = createPrimaryIndexEntry(key);
			cb(key, obj, indexEntry);
		}
		
	};
	
	PrimaryIndex mPrimaryIndex;
	
	Index& Index::getPrimaryIndex() {
		return mPrimaryIndex;
	}
	
	Index::Index(const std::string& name)
	: mName(name), mSequence(0) { }
	
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
				indexEntry.Add('i');
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
	
	inline bool isValid(Driver::Iterator* it, const std::string& indexName) {
		if(!it->IsValid()) {
			return false;
		} else {
			MemSlice keySlice;
			it->GetKey(keySlice);
			Value indexEntry = MemSliceToValue(keySlice);
			
			return indexEntry[0] == 'i' && indexEntry[1] == indexName;
		} 
	}
	
	void Index::remove(Driver* driver) {
		Driver::Iterator* it = driver->CreateIterator();
		
		Value indexEntry;
		indexEntry.Add('i');
		indexEntry.Add(mName);
		
		MemOutputStream out;
        MemSlice keySlice = ValueToMemSlice(indexEntry, out);
		
		it->Seek(keySlice);
		while(isValid(it, mName)) {
			it->GetKey(keySlice);
			driver->Delete(keySlice);
		}
	}
	
	#define KEY_NAME "name"
	#define KEY_SEQUENCE "sequence"
	#define KEY_QUERY "query"
	
	Value Index::toValue() {
		Value retval;
		retval.Set(KEY_NAME, mName);
		retval.Set(KEY_SEQUENCE, mSequence.load());
		retval.Set(KEY_QUERY, mQuery->toString());
		
		return retval;
	}
	
	void Index::fromValue(const Value& v) {
		
		mName = v.Get(KEY_NAME).asString();
		mSequence = v.Get(KEY_SEQUENCE).asUint();
		
		unique_ptr<ViewQuery> query(new ViewQuery());
		query->compile(v.Get(KEY_QUERY).asString().c_str());
		mQuery = std::move(query);
	}
	
	
} // namespace microdb