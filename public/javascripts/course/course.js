var loaded = {};

function courseLoaded(e, request) {
	loaded.course = request.xhr.response.course;
	console.log('done')
	if (loaded.soulutions) initialize(loaded.course, loaded.solutions)
}

function solutionsLoaded(e, request) {
	loaded.course = request.xhr.response.course;
	console.log('done2')
	if (loaded.course) initialize(loaded.course, loaded.solutions)
}

function initialize(course, solutions) {
	// build navigation
	var ol = $('<div></div>').addClass('list-group');
	course.chapters.forEach(function(chapter, index) {
		var li = $('<a></a>').addClass('list-group-item');
		if (index == 0) {
			li.addClass('active');
		} else {
			li.text(chapter.title).click(function() {
				loadChapter(course.chapters[index]);
			});
		}
		li.appendTo(ol);
	});

	loadChapter(course.chapters[0]);
}

<ul class="list-group">
  <li class="list-group-item">
    <span class="badge">14</span>
    Cras justo odio
  </li>
</ul>

<div class="list-group">
  <a href="#" class="list-group-item active">
    Cras justo odio
  </a>
  <a href="#" class="list-group-item">Dapibus ac facilisis in</a>
  <a href="#" class="list-group-item">Morbi leo risus</a>
  <a href="#" class="list-group-item">Porta ac consectetur ac</a>
  <a href="#" class="list-group-item">Vestibulum at eros</a>
</div>