
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
			query->compile("if(obj.id == null) { setValue(obj, \"id\", createUUID() } emit(obj.id, obj)");
			mQuery = std::move(query);
		}
		
	};
	
	PrimaryIndex mPrimaryIndex;
	
	Index& getPrimaryIndex() {
		return mPrimaryIndex;
	}
	
	void indexEmit(Environment* env, Value& retval, const std::vector< Selector* >& args) {
		Value key, value;
		
		const int argsSize = args.size();
		if(argsSize >= 1) {
			args[0]->select(env, key);
			if(argsSize == 2) {
				args[1]->select(env, value);
			}
			Index* index = reinterpret_cast<Index*>(env);
			index->emit(key, value);
		}
		
	}
	
	inline void toMemSlice(MemOutputStream& out, MemSlice& retval) {
		void* ptr;
		size_t size;
		out.GetData(ptr, size);
		retval = CMem(ptr, size, false);
	}
	
	void Index::emit(Value& key, Value& value) {
		MemSlice keySlice, valueSlice;
		
		Value indexEntry;
		indexEntry.Add("i");
		indexEntry.Add(mName);
		indexEntry.Add(key);
		uint64_t sequence = mSequence++;
		indexEntry.Add(sequence);
		
		MemOutputStream keyOut;
		UBJSONWriter keyWriter(keyOut);
		keyWriter.write(indexEntry);
		toMemSlice(keyOut, keySlice);
		
		if(!value.IsNull()) {
			MemOutputStream valueOut;
			UBJSONWriter valueWriter(valueOut);
			valueWriter.write(value);
			
			toMemSlice(valueOut, valueSlice);
		}
		mDriver->Insert(keySlice, valueSlice);
		
	}
	
	Index::Index(const std::string& name)
	: mName(name) {
		SetFunction("emit", indexEmit);
	}
	
	
	
} // namespace microdb