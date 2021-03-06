# es2unix

Elasticsearch API consumable by the command line.

JSON isn't always the most convenient output, particularly on a
terminal.  The tabular format has stuck around for good reason.  It's
compact.  It's line-oriented.  es2unix strives to keep spaces
significant so all output works with existing *NIX tools.  `grep`,
`sort`, & `awk` are first-class citizens here.


# Install

es2unix's only dependency is Java (Oracle or OpenJDK).  Version 7
should be preferred, but there is no functional difference with 6.
Earlier versions aren't supported.

        curl -s download.elasticsearch.org/es2unix/es >~/bin/es
        chmod +x ~/bin/es

# Usage

The `es` command takes subcommands and a few options.  It assumes it's
talking to ES at its default HTTP port using `http://localhost:9200`
but accepts `-u` to change that.  It must be a fully qualifed URL with
scheme, host, & port.

You can also supply `-v`, for most commands, to print a column header.


## Version

        % es version
        es            1.0.0
        elasticsearch 0.21.0.Beta1


## Health

        % es health -v
        cluster status nodes data pri shards relo init unassign
        kluster green      2    2   3      6    0    0        0

## Count

Sometimes you need a quick count to tell whether a cluster has any
data and whether it's indexing.  You can also supply a query.

        % es count
        2,319,799
        % es count elasticsearch
        3 "q=elasticsearch"
        % es count "john deacon"
        225,839 "q=john deacon"
        % es count "\"saved by the bell\""
        220 "q="saved by the bell""

## Search

Not exhaustive access to the query API by any stretch, but it suffices
when you need to get a glance of the data in your cluster.  Searches
across indices with a default query of `*:*`.

        % es search
        1.0     wiki    page 1228929
        1.0     wiki    page 1229142
        1.0     wiki    page 1229146
        1.0     wiki    page 1229153
        1.0     wiki    page 1228943
        1.0     wiki    page 1229155
        1.0     wiki    page 1228950
        1.0     wiki    page 1229159
        1.0     wiki    page 1228956
        1.0     wiki    page 1229160
         Total: 2319799

Can also specify a query, like `es search \"george costanza\"`, and,
possibly more interestingly, a list of fields to return.

        % es search -v "george costanza" title
        score   index  type id      title
        5.78647 wiki   page 660183  George Costansa
        5.78647 wiki   page 273868  George Constanza
        5.63803 wiki   page 865781  Vandelay Industries
        4.69835 wiki   page 932333  Art Vandelay
        4.69835 wiki   page 2147975 Can't Stand Ya
        4.67351 wiki   page 2486208 Art vandelay
        4.07630 wiki   page 2147959 Costanza
        3.23200 wiki   page 2147971 The Costanza family
        3.21007 wiki   page 2147972 Costanza family
        2.94863 wiki   page 4946953 Santa costanza
         Total: 118186

## Master

        % es master
        J-erllamTOiW5WoGVUd04A 127.0.0.1 Slade, Frederick


## Indices

        % es indices -v
        status name   pri rep    docs        size
        green  _river   0   1       4        8068
        green  wiki     1   1 1104894 13805525784

Maybe your cluster is red and you need to know which indices are
affected:

        % es indices | grep \^red
        red    bb           5 0
        red    test         4 1   218b   218  0
        red    enron        5 0
        red    uno          1 0


## Nodes

        % es nodes
        Uv1Iy8FvR0y6_RzPXKBolg 127.0.0.1 9201 127.0.0.1 9300   d Cannonball I
        J-erllamTOiW5WoGVUd04A 127.0.0.1 9200 127.0.0.1 9301 * d Slade, Frederick
        j27iagsmQQaeIpl6yU6mCg 127.0.0.1 9203 127.0.0.1 9303 - c Georgianna Castleberry
        T1aFDU2BSUm748gYxjEN9w 127.0.0.1 9202 127.0.0.1 9302   d Living Tribunal

## Heap

Heap across the cluster.

        % es heap | sort -rnk6
        XO6c2A1D 23.9mb 25138608 123.7mb  129761280 19.4% 127.0.0.1 Junkpile
        uVP8g9_l 94.6mb 99257976 990.7mb 1038876672  9.6% 127.0.0.1 Hammond, Jim
        pjbeg_k8 76.9mb 80730208 990.7mb 1038876672  7.8% 127.0.0.1 Scarlet Centurion

For some quick and dirty monitoring, I like to put this in a loop.

        % while true; do es heap | sort -rnk6 | head -1; sleep 60; done
        XO6c2A1D 57.3mb 60157200 123.7mb 129761280 46.4% 127.0.0.1 Junkpile
        XO6c2A1D 54.7mb 57405904 123.7mb 129761280 44.2% 127.0.0.1 Junkpile
        XO6c2A1D 62.7mb 65834752 123.7mb 129761280 50.7% 127.0.0.1 Junkpile
        XO6c2A1D 56.9mb 59743504 123.7mb 129761280 46.0% 127.0.0.1 Junkpile
        XO6c2A1D 52.1mb 54676216 123.7mb 129761280 42.1% 127.0.0.1 Junkpile
        XO6c2A1D 37.1mb 38971744 123.7mb 129761280 30.0% 127.0.0.1 Junkpile
        XO6c2A1D   52mb 54528424 123.7mb 129761280 42.0% 127.0.0.1 Junkpile
        XO6c2A1D 46.5mb 48787064 123.7mb 129761280 37.6% 127.0.0.1 Junkpile

This can be extremely helpful during indexing, for example.  If you
see a single node showing up a lot, you might have hot shard(s) there.
If you see all the nodes regularly showing up with varying heap usage
percentage, it's likely a healthy cluster with good shard
distribution.

Searching has slightly different characteristics, but you can make
similarly helpful inferences.


## Shards

### Node startup

        % es shards
        wiki   0 p RECOVERING 2.4gb 2634374867 - 127.0.0.1 Bloodshed
        wiki   1 p RECOVERING 2.4gb 2623087542 - 127.0.0.1 Bloodshed
        _river 0 p STARTED    439b         439 0 127.0.0.1 Bloodshed

### Single node filter by index, sort reverse by bytes

        % es shards wik | sort -rnk6
        wiki 1 r STARTED 2.7gb 2980767835 276016 127.0.0.1 Namora
        wiki 0 r STARTED 2.7gb 2953985585 276441 127.0.0.1 Namora
        wiki 1 p STARTED 2.7gb 2909784771 276016 127.0.0.1 Android Man
        wiki 0 p STARTED 2.6gb 2846741702 276441 127.0.0.1 Android Man

### Normal three-node cluster operation

        % es shards -v
        index  shard pri/rep state   size    size-bytes  docs ip        node
        wiki       1 r       STARTED 404.2mb  423845459 28576 127.0.0.1 Cannonball I
        wiki       0 p       STARTED 404.8mb  424543961 28826 127.0.0.1 Cannonball I
        wiki       2 p       STARTED 406.9mb  426734771 28718 127.0.0.1 Cannonball I
        _river     0 p       STARTED 79b             79     0 127.0.0.1 Cannonball I
        wiki       3 p       STARTED 409.1mb  429013649 28761 127.0.0.1 Cannonball I
        wiki       4 p       STARTED 410.6mb  430608757 28819 127.0.0.1 Cannonball I
        wiki       0 r       STARTED 404.8mb  424543961 28826 127.0.0.1 Slade, Frederick
        wiki       2 r       STARTED 406.9mb  426738791 28718 127.0.0.1 Slade, Frederick
        wiki       3 r       STARTED 409.1mb  429017254 28761 127.0.0.1 Slade, Frederick
        _river     0 r       STARTED 79b             79     0 127.0.0.1 Slade, Frederick
        wiki       4 r       STARTED 410.6mb  430611290 28819 127.0.0.1 Slade, Frederick
        wiki       0 r       STARTED 404.8mb  424543961 28826 127.0.0.1 Living Tribunal
        wiki       1 p       STARTED 404.2mb  423851451 28576 127.0.0.1 Slade, Frederick
        wiki       1 r       STARTED 404.2mb  423845459 28576 127.0.0.1 Living Tribunal
        wiki       2 r       STARTED 406.9mb  426734751 28718 127.0.0.1 Living Tribunal
        wiki       3 r       STARTED 409.1mb  429013629 28761 127.0.0.1 Living Tribunal
        wiki       4 r       STARTED 410.6mb  430608737 28819 127.0.0.1 Living Tribunal

# Contributing

es2unix is written in Clojure.  You'll need leiningen 2.0+ to build.

        % make package

# License

        This software is licensed under the Apache 2 license, quoted below.

        Copyright 2009-2012 Shay Banon and ElasticSearch <http://www.elasticsearch.org>

        Licensed under the Apache License, Version 2.0 (the "License"); you may not
        use this file except in compliance with the License. You may obtain a copy of
        the License at

            http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
        WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
        License for the specific language governing permissions and limitations under
        the License.
