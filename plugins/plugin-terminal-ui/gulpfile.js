// include gulp
var gulp = require('gulp');

//todo remove this
//include gulp log
var gutil = require('gulp-util');

/**
 * Clean up lib and build folders
 */
var rimraf = require('gulp-rimraf');
gulp.task('clean', function () {
  return gulp.src(['./lib', 'build'], {read: false})
    .pipe(rimraf());
});

//Todo: FIXME: Add task to typescript validation (ts-lint) code and include it to the main build task.

/**
 * Compile typescript to the lib folder, copy js files to the lib folder
 */
var ts = require('gulp-typescript');
var tsProject = ts.createProject('tsconfig.json');
gulp.task('ts-compile', function() {

  var tsConfigOutDir = tsProject.config.compilerOptions.outDir;//todo use this folder for another gulp tasks
  // var tsConfiSrcDir = tsProject.config.compilerOptions.rootDir;
  // gutil.log(tsConfigOutDir);

  return gulp.src(["src/**/*.ts", "src/**/*.js"])
    .pipe(tsProject(tsProject))
    .pipe(gulp.dest(tsConfigOutDir));
});

var buildDir = './build';

//Include browserify plugin
var browserify = require('gulp-browserify');

// Basic usage
gulp.task('browserify', function() {
  // Single entry point to browserify
  return gulp.src('./lib/xterm.js')//todo try to add fit.js here
    .pipe(browserify({
      debug : true,
      standalone: "Terminal"
    }))
    .pipe(gulp.dest(buildDir))
});

// Copy all CSS files from src/ to $BUILD_DIR/ and lib/
gulp.task("copy-css", function () {
  return gulp.src(['./src/**/*.css'])
    .pipe(gulp.dest('./lib'))
    .pipe(gulp.dest('./build'));
});

//Copy addons from lib/ to $BUILD_DIR/
gulp.task("copy-addons", function () {
  return gulp.src('./lib/addons/**/*.js')
    .pipe(gulp.dest('./build/addons'));
});

//Require gulp-sync-task to sync tasks in gulp process
//Todo in the future: gulp 4(is not released yet) has a feature gulp.series('task1', .. 'taskN') and we will use it instead of gulp-sync-task
var gulp_sync_task = require('gulp-sync-task');

//Main build task
gulp.task('build', gulp_sync_task('clean', 'ts-compile', 'browserify', 'copy-css', 'copy-addons'));

/**
 *  Default task clean temporaries directories and launch the
 *  main optimization build task
 */
gulp.task('default', function () {
  gulp.start('build');
});
