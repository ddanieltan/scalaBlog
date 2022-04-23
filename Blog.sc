import $ivy.`com.lihaoyi::scalatags:0.9.1`, scalatags.Text.all._
import $ivy.`com.atlassian.commonmark:commonmark:0.13.1`

// Bootstrap CSS
val bootstrapCss = link(
  rel := "stylesheet",
  href := "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"
)

// interp.watch broadens `amm -w` to watch changes to both the script and the specified folder
interp.watch(os.pwd / "post")
val postInfo = os
  .list(os.pwd / "post")
  .map { p =>
    val s"$prefix - $suffix.md" = p.last
    (prefix, suffix, p)
  }
  .sortBy(_._1.toInt)

// Building index.html
os.remove.all(os.pwd / "out")
os.makeDir.all(os.pwd / "out" / "post")
os.write(
  os.pwd / "out" / "index.html",
  doctype("html")(
    html(
      head(bootstrapCss),
      body(
        h1("Blog"),
        for ((_, suffix, _) <- postInfo)
          yield h2(a(href := ("post/" + mdNameToHtml(suffix)), suffix))
      )
    )
  )
)

// Parsing md to html
def mdNameToHtml(name: String) = name.replace(" ", "-").toLowerCase + ".html"

for ((_, suffix, path) <- postInfo) {
  val parser = org.commonmark.parser.Parser.builder().build()
  val document = parser.parse(os.read(path))
  val renderer = org.commonmark.renderer.html.HtmlRenderer.builder().build()
  val output = renderer.render(document)
  os.write(
    os.pwd / "out" / "post" / mdNameToHtml(suffix),
    doctype("html")(
      html(
        head(bootstrapCss),
        body(h1(a(href := "../index.html")("Blog"), " / ", suffix), raw(output))
      )
    )
  )
}

