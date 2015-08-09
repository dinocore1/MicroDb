#ifndef SERIALIZE_H_
#define SERIALIZE_H_

namespace microdb {

  typedef uint8_t byte;

  class ValueWriter {
  public:
    virtual ~ValueWriter() {};
    virtual void write(const Value&) = 0;
  };

  class ValueReader {
  public:
    virtual ~ValueReader() {};
    virtual bool read(Value&) = 0;
  };

  class InputStream {
  public:
    virtual ~InputStream() {};

    /**
    * Reads up to max bytes of data from the stream
    * into the buf.
    *
    * returns the total number of bytes read into the buffer
    */
    virtual int Read(const byte* buf, const size_t max) = 0;
  };

  class OutputStream {
  public:
    virtual ~OutputStream() {};

    /**
    * Writes {@code len} bytes from the buf into the stream
    */
    virtual void Write(const void* buf, const size_t len) = 0;
  };

  class UBJSONWriter : public ValueWriter {
  private:
    OutputStream& mOutput;
  public:
    UBJSONWriter(OutputStream&);
    ~UBJSONWriter();

    void write(const Value&);
  };

  class UBJSONReader : public ValueReader {
  private:
    InputStream& mInput;

  public:
    UBJSONReader(InputStream&);
    ~UBJSONReader();

    bool read(Value&);
  };


}

#endif /* SERIALIZE_H_ */
