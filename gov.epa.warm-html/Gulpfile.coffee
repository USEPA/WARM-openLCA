runSequence = require 'run-sequence'
gulp = require 'gulp' 
clean = require 'gulp-clean'
concat = require 'gulp-concat'
jade = require 'gulp-jade'
clientJade = require 'gulp-clientjade'
coffee = require 'gulp-coffee'
stylus = require 'gulp-stylus'
nib = require 'nib'
zip = require 'gulp-zip'
uglify = require 'gulp-uglifyjs'
minifyCss = require 'gulp-minify-css'
fileinclude = require 'gulp-file-include'

gulp.task 'move-to-workspace', () ->
	gulp.src('html.zip')
		.pipe gulp.dest './../gov.epa.warm/resources'

gulp.task 'clean', () -> 
	gulp.src('./target', { read: false, allowEmpty: true })
		.pipe clean()

gulp.task 'views', () ->
	gulp.src('./src/jade/views/**/*.jade')
		.pipe(jade 
			locals: {}
		)
		.pipe gulp.dest './target'

gulp.task 'templates', () ->
	gulp.src('./src/jade/templates/**/*.jade')
		.pipe(clientJade 'templates.js')
		.pipe gulp.dest './target/js'

gulp.task 'scripts', () ->
	gulp.src('./src/coffee/**/*.coffee')
		.pipe(coffee 
			bare: true
		)
		.pipe gulp.dest './target/js'

gulp.task 'styles', () ->
	gulp.src('./src/stylus/**/*.styl')
		.pipe(stylus
			use: [nib()]
		)
		.pipe(concat 'main.css')
		.pipe(minifyCss
				compatibility: 'ie8'
		)
		.pipe gulp.dest './target/css'

gulp.task 'libs-scripts', () ->
	gulp.src([
			'./bower_components/client-jade/bin/jade.js'
			'./bower_components/jquery/dist/jquery.min.js'
			'./bower_components/bootstrap/dist/js/bootstrap.min.js'
			'./bower_components/numeral/min/numeral.min.js'
			'./bower_components/moment/min/moment.min.js'
			'./bower_components/chartist/dist/chartist.min.js'
			'./bower_components/big.js/big.min.js'
			'./other_libs/chartist-plugin-tooltip.min.js'
		])
		.pipe gulp.dest './target/js/libs'

gulp.task 'libs-styles', () ->
	gulp.src([
			'./bower_components/bootstrap/dist/css/bootstrap.min.css'
			'./bower_components/chartist/dist/chartist.min.css'
		])
		.pipe(minifyCss
				compatibility: 'ie8'
		)
		.pipe gulp.dest './target/css'

gulp.task 'libs-fonts', () ->
	gulp.src([
			'./bower_components/bootstrap/dist/fonts/**/*.*'
		])
		.pipe gulp.dest './target/fonts'

gulp.task 'exportable-pages', () ->
	gulp.src([
			'./src/jade/for-export/**/*.*'
		])
		.pipe(jade 
			locals: {}
			pretty: true
		)
		.pipe(fileinclude
			prefix: '//'
			basepath: './target'
		)
		.pipe gulp.dest './target/for-export'

gulp.task 'zip', () -> 
	gulp.src('./target/**')
		.pipe(zip 'html.zip')
		.pipe(gulp.dest('.'))
		.pipe(gulp.dest('../gov.epa.warm/resources'))
	gulp.src('./target/**')
		.pipe(gulp.dest('C:/Users/Nutzer/runtime-WARM.product/html/gov.epa.warm'))

gulp.task 'default', gulp.series('clean', 'views', 'templates', 'scripts', 'libs-scripts', 'styles', 'libs-styles', 'libs-fonts', 'exportable-pages', 'zip', 'move-to-workspace')
