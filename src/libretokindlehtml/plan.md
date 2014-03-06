# Fundamentals
Use json to store a configuration to replace the characters.
Write a key-fn to translate the string names of the functions
into functions.
Also have to merge the files. Have to get rid of extra html, head, body etc. tags and the duplicated stylesheets.

# The configuration file
        A file containing a single JSON object with the following fields:

- __title__: The title of your book. Optional; if set, used by template main.

- __authors__: The authors of the book. A list. Also optional.

- __directory__: Where your files are stored. Currently they all have to be under the same
  directory.

- __order__: A list of all your files, in the order you want them to be when merged.
  If you're not merging, it doesn't matter what order they're in. I thought about various ways to make this automatic, but they all seem pretty creaky (regular expressions are a minefield, and I want it to be possible for authors who can't program to use this, and I don't want to have to debug regexes when I just want to compile my book into an HTML file). Even using Makefile-style aliases doesn't seem worth it because you only have to list the files in this one place, and the program does the rest. Besides, unless you have like a zillion chapters, it's not that hard to type them in, and if you do have like a zillion chapters, you can basically just go to your directory and do python -c "import os; os.listdir(os.get_cwd())" and there you have it, since JSON uses the same syntax as Python for lists. In fact, I might include a Python script that does that with the distribution. (Or a bash script, batch file, blah. In fact I bet it's easy to do that straight from bash, and at least possible from Windows cmd.)

- __merge__: Optional. If true, all the files in "order" will be merged into one file.
  If false or absent, they won't be. [I think scratch merge, because we're using templates now. Plus, to be a real Kindle file it has to be a single file, so just merge by default.]

- __template__: (An array that contains) The path to a file of template and snippet definitions to use on your files (as its first element, and the name of your namespace as the second; using Clojure's symbol function we can dynamically load namespaces from strings, something like (require (symbol (second (:template config)))). Actually if it's on the classpath we don't even need the path. I don't intend this to really be user-configurable; it's more like I'll provide one, and if you really want another one you can figure out how to write Clojure or bitch until I make it for you. Should contain a function called (I dunno, I'll come up with something later, but basically a single function that can be set up with the necessary information from the config file and just kick it and pull everything together into a single file. Like, for me it would be one function that generates all the chapters, the table of contents, title page, and everything, and then sticks it all in a final template. Maybe named something like template-main. It sounds similar to main in Java and C, which it is, but it also shows that we're getting a template, which we are, since the final return value of this function should be the return value of a template.)

- __stylesheet__: The location of a CSS file to use as a stylesheet. If merge is
  true, the stylesheet will be applied to the entire merged file. If merge is
  absent or false, the stylesheet will be applied to each individual file. (This
  is to give me room; the best solution might be to stick the stylesheet in the
  head, or it might be to link it in separately.) [I'll try two options: keeping the stylesheet separate, and sticking the whole thing in the head of the document.]

- __mode__: Optional. Path to a Clojure file with cleanup functions for your generating program. I think I'll standardize on 'maid' as the name for cleanup functions. And if you have different cleanup functions for different templates or snippets they can be that template with -maid, like chapter-maid, paragraph-maid, etc. This is because I like maids. I bet Mahoro-san was programmed in Clojure. (Or Common Lisp, at least. With Vesper's technology, please don't tell me we couldn't do better than C.) The template-main function knows which ones to use with which templates and snippets. But since cleanup functions can be iffy, it's best to scrap as much of the original HTML as you can and just substitute what's necessary into templates or snippets. (I might also try using the snippet and template names as keywords in the mode namespace's metadata map, and use it as a lookup table for the correct cleanup function to use with a given snippet/template.) 

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

1. I think it might be good to put my templates and snippets in a separate novel namespace, because they're not specifically tied to libre office. My cleaner function and selector for the chapter heading can go in the Libre Office file. (Or I might make the chapter heading selector a parameter of the chapter snippet. Then it's easy to vary it by the book. You could even vary it by the chapter if you're weird like that.) Then defining a new cleaning function for e.g. Open Office or Microsoft Word just involves writing a new cleaning function and giving it to chapter when you make a new chapter. [See the new notes on the config file above for how to structure this.]

2. I want a table of contents snippet. Something like the one I made using Enlive to transform the one generated by the Python script. I'd like to use the CSS and move the list to the center of the page, and put "Table of Contents" (or maybe a custom message) at the top in an h2. We'll read the chapter names from the config file (so let's rewrite the snippet to use those as the anchor values. Alternately, we can just use a number, in sequence, as the anchor value, and then use the names from the config file as the names, but link to them by number.

3. Title page snippet. Reads title of the book and author from the config file. Maybe also a dedication page snippet and legal info snippet, with values also to be filled in from the config file.

4. Obviously the main template that brings the whole novel together. Need to make sure it generates correct HTML for realistic situations, so test it on Of Night (without putting that up on the web) and Strawberry Sunflower (maybe).

5. Update the config file according to what will (in a moment) be written above under the section on the config file. Take out mode; the mode is decided in the template file according to what file you include and which functions you give. (Since I went away from being user-configurable and went over to being configurable if you know Clojure and can rewrite that whole part of the program.) As part of this, we need to make the main program read the namespace of the template-main function from the config map so we can use lots of different ones; even if you have to write Clojure code to get new modes, it's important that all your new code can be shoved off into its own space without touching the rest of the code. That will make things more flexible. It also makes sense, given this, to have the tests for the helper functions in the same file with the code.

6. Graphical user interface front-end. Read in the directory, list of files, and other things from the config file from a Swing GUI. Swing isn't so bad, so it shouldn't be hard to make. (There's also a Clojure wrapper for Swing called Quil that might be useful.) The command line interface will accept the path to a config file.

7. Startup script. I want to package it as a JAR when it's done and write both a Linux bash script and a Windows Powershell or batch script to run it. (Macs, I think, can also run bash.)

8. [low priority] Define a snippet for the chapter headings. Then you can redefine the chapter heading snippet. 

9. [lowest priority] Change the name. It's not restricted to Libre Office. This will be rather tedious because I have to change all the namespace declarations; I might look into learning some Awk, or just write a Python script (Clojure is a very parseable programming language, so it actually ought not to be that hard to write a Python script that matches (ns libretokindle[.].+\n, rips off the libretokindle part, and sticks on something else.)) There might be a way to do it in Emacs, too. (But I don't want to make a big production out of this, learning Awk or Emacs, so let's go with Python.)

###Issues

####Dec. 9th 2013:

* Novel template: tested on Of Night under resources folder. Has the following issues:
  * Still weird whitespace from LibreOffice's stupidity. If it's just one nonbreaking space, it goes away, but if you have more than one it doesn't.
  * [solved] Table of Contents links don't work at all. Check how <a> tag is being created. (solved; needed to put # before the id names in the toc entry links to show I was linking to an anchor within the document and not an external file.)
  * [solved] Title page not inserted. (solved; it put in the tags, but since the title and author attributes weren't set, there was nothing to put in, so it's empty and nothing appears. This is correct behavior&mdash;a user assumes if no title or author is given, there won't be a title page, I think.)
  * [solved] Need to replace em dashes with &mdash; and in general escape special characters (it did a number on Erich von Dannekin too. I might just take out that one.) (solved; added &lt;? xml version="1.0" charset="UTF-8" ?&gt; to the top and the tag &lt;meta http-equiv="content-type" content="text/html" charset="UTF-8" /&gt; to the head. Just the meta tag is good enough for the browser, but Kindlegen needs the xml tag too. This makes the text display as UTF-8, so umlauts and em-dashes are fully rendered. Note that the template does not retain the `<?xml version="1.0" charset="UTF-8">` so I consed it manually to the front of the ouput in template-main.)
  * [solved] If :title not set, names the output file .html, making it hidden. (solved; names file "default.html" if title not set.)
  * Some places (e.g. right at the beginning of Chapter 8) don't have newlines. I think this is the same <shift><tab> shit I saw in Strawberry Sunflower that made me switch to Markdown.

####Dec. 10th 2013:
Note: I would work on these problems in roughly this order. These all fall under item 4 on the to-do list above.

* [solved] It cuts off the first sentence of the prologue. No idea why.
  * Cause: the prologue doesn't have a chapter heading, but the insert-heading function just takes the first line, so it cuts off the first line and then does weird things with it. Specifically, it cuts off the first line and tries to select it with the heading selector, but since it's not a heading, it isn't selected and just gets thrown away. Made a separate no-heading snippet that does what chapter does without looking for a heading. I wanted to make chapter use no-heading to insert the body, but there was some insane bug. Probably it was just my stupidity, but finding it seemed like a waste of time so I just copied the code. template-main uses no-heading for the first thing in the list (the prologue) and chapter for the rest of them, that have headings.
* [solved?] Some places (e.g. right at the beginning of Chapter 8) don't have newlines. I think this is the same &lt;shift&gt;&lt;tab&gt; shit I saw in Strawberry Sunflower that made me switch to Markdown.
  * It totally is. These paragraphs are in a single p tag with a br tag inside in the generated HTML. I took out all the br tags inside paragraphs as part of getting rid of whitespace. It looks like it opens a new pointless span, so maybe I can turn that pointless span into a p tag if it comes after a br and unwrap it otherwise.
  * As a quick and cheap hack I tried *not* deleting the line breaks, but then there isn't an empty line between the paragraphs so it still looks bad. Guess I have to go long and expensive.
  * Remember to consider that scene breaks mostly become br tags. Open Office, at least in its Save as HTML from the menu form, puts the br tags inside pointless paragraphs that set the style. Empty paragraphs don't become line breaks, they just disappear.
  * Some progress: this almost works on Chapter 8: (transform c8 {[:br] [:span]} (substitute {:tag :p :attrs {:class "standard"} :content [(-> (first (select c8 {[:br] [:span]})) (select [:span text-node]))]})). It replaces the break and the span inside the paragraph with a paragraph inside the paragraph that contains the relevant content. (At least it's now a paragraph.)
  * Some more progress: this seems to work-- (at c8 {[:br] [:span]} (after {:tag :p :attrs {:class "standard"} :content [(-> (first (select c8 {[:br] [:span]})) (select [:span text-node]))]}) {[:br] [:span]} nil). Stick the new paragraph in afterwards, then delete the break and the original span. To be safe against losing italics and such, we could also put the whole span tag inside the new paragraph and determine whether it's necessary later.
  * For now I'm calling this solved, though the solution was quite specific so I expect it to cause me some trouble later.
  * It wasn't solved. What I had above didn't work because I needed global access to the entire node tree. I now have something that appears to work:  {[:br] [:span]} #(-> % (second) (wrap-standard)), where wrap-standard is a utility function that wraps a tag in p.standard. It wraps the span in another paragraph tag and gets rid of the br tag. Even though the paragraph is inside another paragraph, both Firefox and Calibre's ebook viewer render it like a separate paragraph. So for now let's leave it at that.
* [solved in current form] Carried over: weird whitespace.
  * I found the actual cause of this. Libre Office is stupid about how it italicizes in HTML; it wraps anything in italics with a span and gives that span a class which is italicized by the CSS. It doesn't just end the tag when the italics are over; instead, it frequently creates another span, with another class, with the font set to normal. Any whitespace at the start of one of these tags was not being affected because I was selecting [:> text-node], the child text nodes of any p.standard tag (passed in from chapter to paragraph-maid).
  * I changed the selector to [text-node]. It now gets rid of that whitespace, but the problem is that some of the tags with styles on them also had leading whitespace which was necessary because it was in the middle of a sentence (e.g. `<span class=(italicized)> Century </span>` it gets rid of that first space and we end up with "mid-20thCentury").
  * I can think of two ways to solve this problem. The first is to check whether the span is at the beginning of a paragraph (since that's where we want to remove whitespace) and unwrap the span if it is. Then the whitespace will be removed. The second way is to parse the CSS that LibreOffice generates and figure out which classes are doing something, collapse them all into one class, and unwrap the rest. Obviously the tradeoffs are that the first way is quick, but might cause more problems or ignore some other weird corner case; the second approach will take a long time, but probably won't cause any more pain once it's finally done. I am leaning towards CSS parser, maybe as a separate Clojure library. (Just for the fun. There is another CSS parser on GitHub but it looks kind of rickety.) I also don't want to get trapped in an endless circle of pain with the first solution. Looking at how awful this HTML really is and thinking about how awful it is even having to work with just what was inside the main text, I'm really glad I went with the more elaborate but also more robust route of using templates. And CSS is pretty simple, so writing a parser shouldn't be that bad, especially since I don't even have to parse a lot of the more complex stuff.

####Dec. 11th 2013

* Missing whitespace due to improper application of whitespace removing rule.
  * It seems like this could be solved with a smarter selector. I tried [:p text-node] but it just did nothing (come to think of it, that's because a :p is already selected when it reaches that point). What about text-node first-child?
  * The smarter selectors still run into problems because there's just no way of knowing from nothing but the HTML structure whether a span is necessary or not--at any time, in any context, it could be a useful italic span or a stupid normal font span. Using [:> text-node] passes over any text embedded in spans, so we have some useless spans at the start of paragraphs where the whitespace isn't removed. But what we currently have, just [text-node], catches the spans that are actually italics and removes leading whitespace, even though we usually want it there. So it looks like the CSS parser is the only choice. If we know which spans are doing something and which aren't, we can handle things.
  * I don't want to write a whole CSS parser. What about looking at the style attribute of the head and figuring out which classes are bolded or italicized, selecting them, and setting their style attributes? Then we can tell right from the tag which ones matter (using attr-contains? we can see whether there's a style attribute and whether it contains anything interesting).
  * The very crude beginning of a parser:  (filter #(when-not (= % "") true) (clojure.string/split (first (select c1 [:style text-node])) #"\s")). Needs a different lexing regex because sometimes there are spaces in values (like "font-family: Times New Roman").
  * Have list-of-resources attach the style information as metadata. Give the resources a :style map that lists the selectors and the style information that goes with them. By the way, remember that you can keep the style information as a string, so if you can extract just the class name and whatever's inside the braces, that's good enough.
  * A second, slightly less crude beginning:  (map #(s/split % #"\{|\}") (filter #(when-not (= "" %) true) (s/split (first c1st) #"\n\t"))). It splits the style tag's contents into lines (Libre Office puts \n\t before every entry), then filters out all the empty strings, and splits the lines at open or closing braces. This leaves us with a sequence of vectors containing two items: the list of selectors and the style to apply (both as strings).
  
    Example: (["@page " "  "] ["table " " border-collapse:collapse; border-spacing:0; empty-cells:show "] ["td, th " " vertical-align:top; font-size:12pt;"] ["h1, h2, h3, h4, h5, h6 " " clear:both "] ["ol, ul " " margin:0; padding:0;"] ["li " " list-style: none; margin:0; padding:0;"] ["<!-- \"li span.odfLiEnd\" - IE 7 issue-->"] ["li span. " " clear: both; line-height:0; width:0; height:0; margin:0; padding:0; "] ["span.footnodeNumber " " padding-right:1em; "] ["span.annotation_style_by_filter " " font-size:95%; font-family:Arial; background-color:#fff000;  margin:0; border:0; padding:0;  "] ["* " " margin:0;"] [".P1 " " font-size:12pt; font-family:Times New Roman; writing-mode:page; font-style:normal; "] [".P2 " " font-size:14pt; font-weight:bold; margin-bottom:0.212cm; margin-top:0.423cm; font-family:Times New Roman; writing-mode:page; text-align:center ! important; "] [".Standard " " font-size:12pt; font-family:Times New Roman; writing-mode:page; "] [".T1 " " font-style:italic; "] [".T2 " " font-style:normal; "] [".T3 " " vertical-align:super; font-size:58%;font-style:normal; "] ["<!-- ODF styles with no properties representable as CSS -->"] ["" " "])

    * For the next stage: (filter #(let [[_ styles] %] (when (and styles (re-find #"(font-weight:bold)|(font-style:italic)" styles)) true)) plist) on the output of the previous. This finds all the vectors that have two items and have a bold or italic in their second item (the style string). Then we can parse out the selector string (in most cases I expect it'll just be a single class) and turn all the matching elements into keywords, which we can then use in Enlive selectors to set the style attribute.

(def with-keywords (map #(let [[class style-str] %] [[(keyword class)] style-str]) needed_styles))
needed_styles is the output of the last bit of code (filter ...) from above.

####Dec 12th, 2013:

* The style stuff looks good so far, except that for some mind-numbing reason the select function won't accept the selectors that we get from the (def with-keywords.. code above. To be more accurate, it accepts them, but it returns empty, while typing in directly *the exact same selector* works, as does every other mangling I've tried, like (select c1 [(keyword ".T1")]). For some reason the selector I read from the list doesn't compare equal with a literal selector either. The only (totally idiotic) clue I have is that the repl prints the read selector with a space after the keyword, like this: [:.T1 ], while literal selectors are printed without this extra space.

    I want to try converting the string into a selector and straight away trying to select something with it without first storing it somewhere, but the repl is being fucking retarded and I'm too tired to mess with it.

    Here's the rub: Clojure keywords are really weird. The spec says they don't allow certain characters, but in practice they basically allow anything.
    The strings I was reading from the file had spaces at the end, like this: ".T1 ". When I turned them into keywords, I didn't realize that the space became part of the keyword. So there was actually a difference between :.T1 and :.T1\u0020 with a space after it! Because of this, they didn't compare equal. Since Enlive is pretty permissive, it didn't barf or anything; it just searched for the class "T1 ", which nothing in the document had, so the return value was just an empty list.
    So changing the code to this solves the problem:
    (def with-keywords (map #(let [[class style-str] %] [[(keyword (trim class))] style-str]) have-style))
    Then we can transform the content like this:
    (reduce #(let [[selec style-str] %2] (transform %1 selec (set-attr :style style-str))) paragraph with-keywords)

    * To integrate this into the program
      * In list-of-resources, get the info and pare everything down to the with-keywords list (I'm going to first try dumping all the styles except those with bold or italic. If it doesn't work I'll just use all the styles.) I want to do this per-file because I don't want to assume that Libre Office is consistent across files. Then the styles list will be saved as metadata on the file along with the name and position.
      * Integrating with paragraph-maid is a bit of a challenge. What I think I might do is compose that reduce above (along with something to grab the style info from the metadata) with the forms inside the transformation function to create a function that takes one argument. (I can rewrite it as (at % ...) instead of (transformation ...).) And then use that as the cleaner function. I might also rewrite the whole cleaner function to take an argument (but then I'd have to rewrite everything I have so far, so maybe not.)
      * Another possibility is to apply it above the cleaner, in chapter and no-heading. This kind of makes more sense because I'm not actually cleaning up the paragraph, I'm just moving around the style information. I could write a utility function and add in the style information right before it goes into the cleaning function.
      * Yet another possibility is to add it in list-of-resources or mine-content (or mine-all). This makes the most sense since the style info isn't really part of your template. On the other hand, I feel like this ties those (supposedly general) functions to a specific use case. What if you want to ditch all the style information? What if you don't want to ditch the head of your document? Since novel and libreoffice are both sort of replaceable.
    * On another note, I think I know how to get rid of no-heading. Inside insert-heading, test whether we matched anything with headtext. If we did, it's the heading. Proceed as before. If not, we have to somehow alert the next clause not to cut off the first line. This is relevant because right now I'm thinking about making changes to chapter to add in styles, and anything I change in chapter has to also be changed in no-heading, the usual disadvantage of code duplication. So I'm trying to not duplicate it.

###December 14th, 2013:

* Still working on getting styles integrated. 
  * There's a whole fuckaround in merge_files having to do with things being opened as Enlive resources. I tried to write a function that does all that (html-resource (file ..)) bullshit for you but it somehow can't find files and shit now.
  * It inexplicably started working. All I can figure is that the code was somehow outdated and I did something that reloaded it without realizing it. At least, the style functions now pass the tests I gave them; I still have to make them actually work with list-of-resources.
  * The style functions work with list-of-resources. (I'm writing tests for it right now). Just keep this in mind: the way of parsing the stylesheets that you have going is an absolute mess. It doesn't work on anything except the simplest case, where you have one selector only. It includes comments and other crap in the style list. It's basically garbage. But it should work for just what I want right now. But if you try to use this crap for anything even marginally more complex, it will break hard. The only way to stop this is to go all out and write a parser for CSS (or use that one that the VimClojure guy wrote). Two main issues: you don't parse the selector list so if there's a bunch of them, like h1, h2, h3, blah blah, it doesn't make them all into keywords in a selector; it makes a single keyword with spaces in it. Also, it shoves everything in a vector without regard for whether the original CSS wanted a hierarchy [:ul :li], an intersection [[:ul :li]] or a union #{:ul :li}. There's a CSS grammar online if you decide to write a parser; it's probably easily susceptible to predictive parsing, but Antlr might also be of some use.
  
###December 15th, 2013:
* Within paragraph-maid, filter the list of styles in the paragraph metadata to get just the ones with bold or italic in the names. (Or possibly in make-paragraphs? Use another helper inside make-paragraphs?) Yes, inside make-paragraphs. Use the above code to either:
  1. Attach the style string to each tag that needs it.
  2. Change the classes of all the tags to get the names the same. I don't trust Libre Office to be consistent across documents (i.e. I know that Chapter 1 of Of Night uses T1 as the class for italics, but I don't know that Chapter 2 doesn't use T2 or P3 or Chicken for the same thing). If I knew I could trust all the italics to be the same class, I could just italicize it in my main stylesheet. I kind of like that better. (Also, dealing with that crap from Libre Office is a pain; I want to throw out as much of it as possible, just like with the templates.) So change all the classes to be the same class and then make it italicized in the novel_style stylesheet.

* Since I already know that calling extract-styles with a regular expression does what I want, and I just want to get this damn thing working for now, I'm just going to have list-of-resources call it with a regular expression. Then I'll have no-heading and chapter collapse the classes and see if it works. From there we can think about making things less intertwined. Since I resisted the temptation up to now it shouldn't be that bad. (This style thing needs major fixing anyway. I'm just going to do the quick and easy way and then go back and try to write a CSS parser to fix it.)

* I am currently trapped in a bind that has no apparent exit.
  * Enlive doesn't seem to have any way to work with text nodes well. This is the same as CSS. So there seems to be basically no way to apply the initial whitespace deletion only to the beginning of a paragraph; it's all or none. I thought unwrapping the pointless spans would fix the problem but it doesn't because the contents of the pointless spans are still considered separate text nodes by Enlive. Either I lose all the whitespace that was at the beginning of those pointless spans, or I get pointless whitespace at the beginning of my paragraphs wherever Libre Office felt like putting it in.
  * So I am backtracking a bit in my design. (Also to deal with the style issue.)
  * First of all, I don't want to write custom cleaner functions. I want to make it user-scriptable with Enlive selectors and functions. So that part is back on.
  * Second, I think it would be good to distinguish between structural cleaning and textual cleaning. Structural cleaning is stuff like getting rid of pointless spans and fixing <br> tags that come from shift return. Textual cleaning is cleaning up the shit that Libre Office does to the text itself, like putting in pointless whitespace. Structural cleaning also covers ensuring that the styles remain intact.
  * Structural cleaning can mostly be done with Enlive like I have been.
  * Textual cleaning will involve getting the content out with Enlive and then possibly lexing or parsing it to whip it into shape. Three major things need to be done at this stage:
    1. Make sure there's no pointless whitespace (defined as any whitespace other than the spaces between words).
    2. Make sure the styles are all applied as they should be.
    3. Get rid of spans and divs that don't do anything (have no style rules applying to them, have style rules that make the weight or style normal, etc.)
  * I think I vastly underestimated the horror of that HTML (even though I knew it would be horrible, I didn't realize just how cut down it would need to be to be manageable). You might consider writing a whole separate module to just whip the paragraphs into shape.

####Practicalities
* Do all textual cleaning first, then structural. Probably this can be accomplished with two transformation functions, one that does the structural cleaning and another one that does the textual cleaning. A third function

###December 17th, 2013

You know what would be super-awesome? Make the damn thing scriptable. In Rhino.

Instead of having to translate weird config file syntax into cleaning functions and transformations, expose all the Enlive functions through some kind of Rhino interface (maybe by compiling them to Java classes) and then make it possible for a user to just write a file of Rhino code, a braindead script like writing everything at the top level in Python, including the transformations the user wants in the order the user wants them. Expose the file list, too. And Javascript has first-class functions so users could also write their own cleaner functions just like you can do with Enlive in Clojure.

I'd have to look into a way to package all the data up and send it off to the Clojure program.

It sounds pretty difficult now that I look into it, so this might be the best way to do it: still parse and all that, but instead of making up some special format that you have to write a parser for, make it Javascript. Something like, define a Java or Javascript class that has the same interface as Enlive. But it delegates everything to Clojure by transforming it to JSON and sending it off to the Clojure process.


###December 18th, 2013:

Right now I'm using Webbit to stick on a web interface. Just because it's fun, I want to learn something about the web, and it's most likely nicer than using the command line.

I think it would be cool to send the files over the client as a zip file and have Clojure unzip them and work on them. Then Clojure can send the finished file back to the client. JQuery can upload (and presumably download) files, although I can't find out how right now because my internet is being stupid, so this should be feasible. (It uses Ajax, so maybe I can figure it out on my own.) If I get it working I might try doing it across two of my computers.

I can have Javascript open another tab and load the downloaded file in there, and even let users apply different stylesheets to see which one they like the best. Then I can allow downloading of the file along with a stylesheet as a zip file.

### December 19th, 2013:

Issue: The program sticks the main text inside the front_matter div, so all the styles that apply to the front matter also apply to the main text. Most notably, since my front matter is centered, so was my main text. I just overrode it by directly styling the main text to be left-justified; I also went and moved the front matter's closing tag. But it needs to be fixed for this program to be useful. (That's sort of a joke; this was more of a one-time program. Most people don't need their files merged because they just write it as one file to begin with, and they're also not bothered by the crap HTML that Microsoft Word and Libre Office spit out.)

Note: I've been trying to figure out why my subtitle has a page break before it. Now I know: it's all Calibre's fault. It dumped my stylesheet and made its own, which inexplicably put a page break before my subtitle (because it lumped everything into its own classes instead of using mine). 

### January 25th, 2014:

Got Of Night published, but now I'm trying to make it work for Strawberry Sunflower, which has its own set of problems (like inexplicable pointless newlines—wait, I know where they came from: Libre Office added them to make the HTML look nicer). I used the "Save as HTML" option from the menu, and as we discovered last time, that and the command line converter do not ruin the text in exactly the same way.

We have whitespace in the list of resources. That crashes make-paragraphs, which expects its arguments to be callable. The files have no style attribute, so style-string returns nil and crashes extract-styles. Everything is too much tied to Of Night.

I think after all I do like the idea of writing your own file of Clojure code to clean and convert everything. Too much of what's in that file is specific to Of Night. Besides, let's be realistic: only I will ever use this. (And not even me, for much longer; after Of Night 2, I'm switching to Markdown. Emacs is sort of ugly for plain text but anything is better than this. Or I might write in Open Office, save as plain text, and use Enlive to populate the template.)

## January 27th, 2014:

I wrote a Python script that drags down a table of HTML entities with their Unicode equivalents and stores it in a CSV. Clojure can parse CSVs with a library on GitHub, so do that and do post-processing where you search for all the characters and replace them with the entity ref version (e.g. — gets replaced with &mdash;). I did this in Emacs with Of Night, so now I'll do it automatically.

## January 28th, 2014:

Strawberry Sunflower builds now. The problems we had were:

* I stupidly mis-cased the directory name (it's "Magical 4koma **s**tory", not "**S**tory").
* The Strawberry Sunflower files didn't have anything in their style tags, so the style extracting functions were returning nil. I modified the function to check whether its argument was nil and return an empty string if so; this way it works just like if we had a style string, but nothing happens to the style.
* The files also had whitespace (newlines) that got extracted by mine-all into the list of resources. We then passed that list to make-paragraphs, which expects to be able to look up things in its arguments. Since it couldn't, we got ClassCastException. I fixed this by having chapter filter its list of raws and only include maps (using map? as the filtering predicate). I thought about using fn? but I can't see ever wanting to use a function there.
* The headings didn't get filled in properly. The selector I was using for Of Night headings didn't work with Strawberry Sunflower. I fixed this by changing the structure slightly; the extract-heading function was designed with the assumption that the heading would always be the first line. I knew when I wrote it that this assumption wasn't tenable, but I just wanted to get the book built. Now the extract-heading function just finds a paragraph that fits its selector; the heading has to be uniquely defined by its selector, and the function throws an exception if that's not the case. The chapter template now forms a list of raws by selecting just the paragraphs that fit a standard paragraph selector that you also have to write.

I think that particular design is all right (though I could see having a book that's so broken you can't uniquely define a selector that gets just the header, and another that gets just the standard paragraphs), but I had to modify the code a lot. Fortunately, since I was sort of thinking about user customization, the places I had to modify were pretty limited; they were all in the novel.clj file. I also wrote a new cleaner function inside libreoffice and modified novel.clj to use it. (By the way, in Strawberry Sunflower all the spans were pointless because it used <i> and <b> tags for italic and bold, so I could just unwrap them all, as well as all the pointless font tags. That was pretty nice; the HTML looks pretty clean now.) I added the select-standard-paragraphs variable, and I changed the novel template to not call no-heading first since all the chapters in Strawberry Sunflower have headings. But to make the program work well, there has to be some kind of way to customize these things. (If I were reading data defined by a user, I would just enforce a particular format convention and we wouldn't need the customization. But I'm reading in awful HTML created by word processor programs.)

I kept jumping around on how to handle these customizations, because some of them seem to require new code (like the cleaner function and making new templates) and others can be handled pretty simply with flags in the json config (like the heading/noheading rule—you could just have a boolean variable "heading on first?").

I might like to use Clojure's read-string/eval. Since it's user-inputted data and it's not online where malicious users can get at it (it's just a desktop app. Even my web interface doesn't seem to be happening, and that was just for fun anyway). That can handle the heading issue; you'd do this:

{
    "select-standard-paragraph": "[[:p (attr= :class "standard")]]",
    "select-header": "[[:h2 first-of-type]]",
    "no-heading": "#{\"Chapter 1\", \"Chapter 29\"}"
}

Then Clojure can read-string those, store them somewhere, and eval and use when needed. That just leaves the cleaner function. I wonder if we could do something like this:

    "cleaning": ["[:p :span] unwrap", "[text-node] (do-> #(clojure.string/replace % #"^\s+" ""))"]

(or as one long string) and pass that to transformation (which is a macro, so you can just pass it stuff without evaling it, I assume, and if not, we can probably still eval it and pass it in).

With that approach, it might be good to dump the JSON; I liked learning about it, but it seems to make more sense to have a file of Clojure code that contains a config map in a Clojure map structure (or possibly a record, which can be defined elsewhere, to cut down on validation). Then we can read-string the whole map/record and get it that way. In retrospect I feel stupid for not thinking of that earlier since it is a Lisp power to be able to read and execute your own data structures as code like nothing. That way when we write the selectors and cleaner function, we have a whole Clojure environment going on and minimize processing. So we could just do:

{
    :heading-selector [[:p first-of-type]],
    :paragraph-selector [[:p (attr= :class "standard")]],
    :cleaner (fn [] (transformation ...))
}

I think that sounds good.

Note: we don't need no-heading anymore. If we have a heading selector that's uniquely defined, then in a chapter with no heading, nothing should be selected by that selector. Then it'll just fill in an empty string, like it was doing with Strawberry Sunflower before I changed it to get the headings, and we'll have no heading. 
