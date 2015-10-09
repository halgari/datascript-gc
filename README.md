# datascript-gc

A simple garbage collector for Datascript. Given a list of EIDs and attributes to follow, this
library will generate a transaction that will remove all non-referenced data. Useful for situations
where a branch of a tree has been detached and should be removed for improved memory usage. Probably
a really bad idea to do this with Datomic, but for Datascript it doesn't matter so much since it
doesn't track history.

## Usage

The tests show some example usage, but you probably shouldn't use this.

## License

Copyright © 2015 Timothy Baldridge

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
