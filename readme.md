# No name yet

A Clojure program to clean up the HTML output from Libre Office and make
it suitable for publishing on the Amazon Kindle Store. I intend to read
the instructions for cleaning up the HTML (i.e. which tags to remove,
which attributes to add or remove, what to rename classes, etc) from
a file as JSON, so even though I'm just working on Libre Office right now
I also want to write files to fix Word and other word processors' lousy
HTML. Will also have the capability to merge several HTML files into a
single document; I don't know about you, but I kind of like having each
chapter in its own file so it's easy to navigate them.

## Usage

Not implemented yet. The intention is to have the program read the directory
where you files are and the name of your config file, full of JSON,
from the command line. The sophisticated options will all go in the JSON
file; they'll let you do things like merge all files under a directory
into one file, get rid of extraneous tags like all those useless extra
span and div tags that word processors throw in, and change the attributes
so you can give your paragraph classes descriptive names instead of
"P1".

When it's closer to being done, I'll put up an example JSON config file
and some more explanation of the options.

## License

I don't know why you'd bother to steal this code; a thousand monkeys on a thousand
terminals could probably do it better, and that's aleph-null monkeys too few for the infinite
monkey theorem to guarantee you'll ever get the program you want. Still, for the sake of
completeness, let's say this is under the Eclipse Public License, the same as Clojure.
It's (c) and (r) Trisdan Leyson, all rights reserved, yada yada. Take it, modify it, expand it,
just don't sell it without kicking some back to me. That's all.
