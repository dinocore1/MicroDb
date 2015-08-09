
ViewQuery has internal atomic counter that can be read/incremented by external
api. This allows code using microdb to increment the counter and then embedded
the result in a document that it is about to be inserted. This mechinism
can be used to provide a natural record order. The atomic counter must
initialized to the number of documents in the query.

changes to the database can be divided into two categories: insertions and
deletions. An update is really just a delete followed by a insert of a document
with the same ID. Tracking changes between checkpoints can be done by keeping
a log of all the document IDs that were inserted and deleted. Keeping the log
can be done by using an index:

[next checkpoint uuid][doc id][insert OR delete] --> [document]

reverting to an earlier checkpoint is achived by iterating all the doc IDs
that changed and performing the oppisite operation on the database. For example,
if a document was recored as being inserted in the checkpoint index, then
the proper revert action is to delete the document from the database. And vise,
versa
