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
