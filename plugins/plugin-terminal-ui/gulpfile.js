// include gulp
var gulp = require('gulp');

// include plug-ins
var jshint = require('gulp-jshint');

// JS hint task. This task checks quality javascript code.
gulp.task('jshint', function() {
  gulp.src('./lib/*.js')
      .pipe(jshint())
      .pipe(jshint.reporter('default'));
});

/**
 * Clean up lib folder
 */
var rimraf = require('gulp-rimraf');
gulp.task('ts-clean', function () {
  return gulp.src('./lib', {read: false})
    .pipe(rimraf());
});

/**
 * Compile typescript to the lib folder, copy js files to the lib folder
 */
var ts = require('gulp-typescript');
var tsProject = ts.createProject('tsconfig.json');
gulp.task('ts-build', ['ts-clean'], function() {
    var tsResult = gulp.src(["src/**/*.ts", "src/**/*.js"])
        .pipe(tsProject());
    return tsResult.js.pipe(gulp.dest());
});


