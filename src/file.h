#ifndef FILE_H_
#define FILE_H_

#include <string>

namespace microdb {
	
	class File {
		private:
		std::string mPath;
		
		public:
		File(const std::string& path);
		File(const File& parent, const std::string& path);
		
		bool IsDirectory();
		std::vector<File> ListFiles();
		
		bool IsFile();
		bool Exists();
		bool Delete();
		
		std::string GetAbsPath();
		std::string GetName();
		
		File GetParent();
		
		static void deleteTree(const File& dir);
	};
	
}


#endif // FILE_H_