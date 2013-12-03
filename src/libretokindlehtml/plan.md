# Fundamentals
Use json to store a configuration to replace the characters.
Write a key-fn to translate the string names of the functions
into functions.
Also have to merge the files. Have to get rid of extra html, head, body etc. tags and the duplicated stylesheets.

# The configuration file
A file containing a single JSON object with the following fields:

- __directory__: Where your files are stored. Currently they all have to be under the same
  directory.

- __order__: A list of all your files, in the order you want them to be when merged.
  If you're not merging, it doesn't matter what order they're in.

- __merge__: Optional. If true, all the files in "order" will be merged into one file.
  If false or absent, they won't be.

- __transform__: A nested object whose keys are Enlive tag selectors and
  whose values are Enlive transformation functions. If merge is true, the
  transformations will be applied to the merged file, not to the individual
  files before merging.

- __stylesheet__: The location of a CSS file to use as a stylesheet. If merge is
  true, the stylesheet will be applied to the entire merged file. If merge is
  absent or false, the stylesheet will be applied to each individual file. (This
  is to give me room; the best solution might be to stick the stylesheet in the
  head, or it might be to link it in separately.)

- __mode__: Optional. Give a name to the mode defined by this object, e.g. "LibreOffice",
  "MicrosoftWord", "Wikipedia".

I wrote all that, but I think now it's easier to just specify some Clojure functions to
do the transformations. It's user-extensible if you know Clojure. For now I'm the only
one using this so it doesn't really matter if anyone else knows how to do it, and I
know Clojure so I can get by. Maybe the transformation functions should be stored in a
file "mode.clj" where mode is the mode property of the json object.

I think there is a nicer way to do a lot of this using templates and snippets. Instead of just transforming the crappy HTML output, mine the content from it and put it into a template that makes it nice and properly formatted. Make snippets for page breaks, normal paragraphs, chapter headings, and scene breaks. The HTML output ought to be consistent enough to get those things out of the document. (Maybe that could be where the user comes in; after all, selector syntax isn't that hard [weird, but not extensive] and a selector is just a vector, which can be saved in a variable. Then the functions given to transform the selector can be the snippet name.

####Pros and cons

#####Transform original document:

######Pros

* Stylesheet from original document stays intact.

* Don't have to learn how to use templates and snippets.

######Cons

* Might have horribly interacting flaws. By which I mean there might be some aspects of the original output that work against each other to make transforming it as-is almost impossible without implementing AI techniques.

#####Put stuff in a template:

######Pros

* Lots of control over containers. Since I define the paragraphs and chapters and all that as snippets, I never have to worry about how they'll interact with whatever transformations I want to do on the body text because I have complete control over how they're made. (e.g. I can make sure they all have unique ids and so on.)

* More control in general. I control the stylesheet and metadata and all that. I also control how I might implement things like chapters with multiple sections.

* Cleaner. Get rid of a lot of the junk in the original document.

* Once it's done, I might have to adjust some of the transformation code for the content inside the paragraphs, but I'll never have to worry about global properties of the document or weird little corner cases I didn't notice when I first wrote the program.

######Cons

* More complicated. I have to learn about templates and snippets.

* Might be too restrictive.

#####Conclusion

For now I'm going with templates. I almost had them figured out last night so learning them shouldn't be that hard. I'll put the code in a branch on GitHub.

The reason is that if templates don't work out, it will probably be obvious very soon why they don't work. Then I can stop and go back to customized transformations. If I start with custom transformations and they don't work out, it'll probably be because of some horribly interacting flaw and I'll waste a lot of time trying to solve it before giving up and going to templates.
