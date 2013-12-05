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
  If false or absent, they won't be. [I think scratch merge, because we're using templates now. Plus, to be a real Kindle file it has to be a single file, so just merge by default.]

- __template__: The path to a file of template and snippet definitions to use on your files. I don't intend this to really be user-configurable; it's more like I'll provide one, and if you really want another one you can figure out how to write Clojure or bitch until I make it for you. Should contain a function called (I dunno, I'll come up with something later, but basically a single function that can be set up with the necessary information from the config file and just kick it and pull everything together into a single file. Like, for me it would be one function that generates all the chapters, the table of contents, title page, and everything, and then sticks it all in a final template. Maybe named something like template-main. It sounds similar to main in Java and C, which it is, but it also shows that we're getting a template, which we are, since the final return value of this function should be the return value of a template.)

- __stylesheet__: The location of a CSS file to use as a stylesheet. If merge is
  true, the stylesheet will be applied to the entire merged file. If merge is
  absent or false, the stylesheet will be applied to each individual file. (This
  is to give me room; the best solution might be to stick the stylesheet in the
  head, or it might be to link it in separately.) [I'll try two options: keeping the stylesheet separate, and sticking the whole thing in the head of the document.]

- __mode__: Path to a Clojure file with cleanup functions for your generating program. I think I'll standardize on 'maid' as the name for cleanup functions. And if you have different cleanup functions for different templates or snippets they can be that template with -maid, like chapter-maid, paragraph-maid, etc. This is because I like maids. I bet Mahoro-san was programmed in Clojure. (Or Common Lisp, at least. With Vesper's technology, please don't tell me we couldn't do better than C.) The template-main function knows which ones to use with which templates and snippets. But since cleanup functions can be iffy, it's best to scrap as much of the original HTML as you can and just substitute what's necessary into templates or snippets. (I might also try using the snippet and template names as keywords in the mode namespace's metadata map, and use it as a lookup table for the correct cleanup function to use with a given snippet/template.)

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

Figure out how the chapter headings, italicized segments, etc. are represented by the Libre Office html and make a Clojure file that defines selectors for all of them, either as a map (that can be used as a function) or as named vars, so we can call them 'chapter', 'italics' as semantic elements. This will also make it easy to make files for new kinds of HTML output.


# December 5th, 2013

The template idea looks like it was a really good idea. Unnesting all that other crap and sticking the text into my nice, clean template has made lots of things simpler. I have the chapter snippet looking good, and while I'm sure there's more I could add to the cleanup function, I'll figure that out as I go. The whole point of making this general instead of specific was to make it easy to experiment with different ways of cleaning the data and find the one that works best for me.

To-do items (roughly in order of priority, from high to low, and also in order of dependency on each other):

* I think it might be good to put my templates and snippets in a separate novel namespace, because they're not specifically tied to libre office. My cleaner function and selector for the chapter heading can go in the Libre Office file. (Or I might make the chapter heading selector a parameter of the chapter snippet. Then it's easy to vary it by the book. You could even vary it by the chapter if you're weird like that.) Then defining a new cleaning function for e.g. Open Office or Microsoft Word just involves writing a new cleaning function and giving it to chapter when you make a new chapter. [See the new notes on the config file above for how to structure this.]

* I want a table of contents snippet. Something like the one I made using Enlive to transform the one generated by the Python script. I'd like to use the CSS and move the list to the center of the page, and put "Table of Contents" (or maybe a custom message) at the top in an h2. We'll read the chapter names from the config file (so let's rewrite the snippet to use those as the anchor values. Alternately, we can just use a number, in sequence, as the anchor value, and then use the names from the config file as the names, but link to them by number.

* Title page snippet. Reads title of the book and author from the config file. Maybe also a dedication page snippet and legal info snippet, with values also to be filled in from the config file.

* Obviously the main template that brings the whole novel together.

* Update the config file according to what will (in a moment) be written above under the section on the config file.

* Graphical user interface front-end. Read in the directory, list of files, and other things from the config file from a Swing GUI. Swing isn't so bad, so it shouldn't be hard to make. The command line interface will accept the path to a config file.

* Startup script. I want to package it as a JAR when it's done and write both a Linux bash script and a Windows Powershell or batch script to run it. (Macs, I think, can also run bash.)

* [low priority] Define a snippet for the chapter headings. Then you can redefine the chapter heading snippet. So we'll have two user-configurable files: 
