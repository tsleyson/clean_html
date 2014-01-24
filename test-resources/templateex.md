# Enlive template example

The files test-resources/application.html and test-resources/header.html define our template and snippet.

##The snippet

Snippets are basically definitions for working with pieces of html. You define one using enlive/defsnippet. You pick what kind of piece to work with using the selector argument.


(enlive/defsnippet poplinks (file "test-resources/header.html") [:header]

The name of the snippet, the file where your substitution form is declared, and the containing tag for your substitution form. Using [:header] tells us that this snippet will be doing all its tag retrievals under the header tag. (Or tags; I imagine if there's more than one it selects all of them.)

[heading nav-elts]

The argument list. This snippet takes two arguments, the title for the heading, and the vector of vectors that contains pairs of captions and urls for the items you want in the navigation list in this snippet.

[:h1] (enlive/content heading)

[:ul [:li enlive/first-of-type]] (enlive/clone-for [[caption url] nav-elts] [:li :a] (enlive/do-> (enlive/content caption) (enlive/set-attr :href url))))

The rest is a list of selectors and transformations evaluated inside an implicit at. All of the selectors are implicitly under heading. The first transformation finds [:h1] (if we had other h1 tags inside, we could instead write [:h1 first-of-type] to get just the first h1 tag under header) and replaces its content with heading.

The second transformaton finds every ul tag and selects the first li under it. clone-for iterates over the entire list of nav-elts, binding them to caption and url, and taking the a tag under li as its model, makes a copy for each nav-elt, sticking in the caption and url. By the way, the :li inside the clone-for selector seems to be redundant (this example comes from that horrible quickstart guide in the Enlive readme, which seems to be not just confusing, but actually wrong in many cases). It seems that you can just as well write [:a] inside the clone-for and it works, because it's already set up to pass in just the first list element under a ul by the selector inside defsnippet. But there may be some subtlety I'm not seeing. (BTW, the original example was even worse: it had the selector for [:li :a] written out twice for each of the actions inside the do->.)

This is the whole thing.

(enlive/defsnippet poplinks (file "test-resources/header.html") [:header]
    [heading nav-elts]
        [:h1] (enlive/content heading)
        [:ul [:li enlive/first-of-type]] (enlive/clone-for [[caption url] nav-elts] [:li :a]
        (enlive/do-> (enlive/content caption) (enlive/set-attr :href url))))

##The template

Templates are like snippets, but they don't pick just certain elements to work on; they take the whole document as a substitution form.

defsnippet returns a function which will do the given transformations on a provided node tree and return a node tree. deftemplate returns a function which will do the same, but return a sequence of strings that make up the lexemes of the generated html. So you actually shouldn't do emit* on the return value of a template; just do (apply str) on it and it'll come out.

Here's a template:

 (enlive/deftemplate maintempl (file "test-resources/application.html") [title heading nav-elts] [:head :title] (enlive/content title) [:body] (enlive/content (poplinks heading nav-elts)))

(enlive/deftemplate maintempl (file "test-resources/application.html")
    [title heading nav-elts]

Notice how there's no selector after the file like with defsnippet.

[:head :title] (enlive/content title)
[:body] (enlive/content (poplinks heading nav-elts)))

These select the title under the head tag and set its content to the title argument, and select the body and replace its content with the return value of poplinks with the heading and nav-elts arguments. That means we pass the whole body into that function. Since poplinks is a snippet function, it selects the header tag under the body and then applies its transformations. If we had multiple snippets to apply, we could use do-> to chain them all together.

It seems to me that a good way to work with templates might be to define a template that applies a bunch of snippets to its head and body sections. If we wanted to do something fancier with the head, we could have had a snippet that selects the title tag and populates it, and another that selects the meta tag and populates it, and another for the link and style tags.  Under the body we could have a snippet that populates a nav menu, another that does a picture menu, another that does a fine print section at the foot, etc.
