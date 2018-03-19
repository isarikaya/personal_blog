$('.btn-file').on('click', function () {
	$('input[type="file"]').click();
	$('input[type="file"]').on("change", function () {
		var $crudProgress = $("#crud_progress div");
		if ($crudProgress) {
			$crudProgress.css("width", "0%");
			$crudProgress.text("0%");
		}
	})
})
$(function () {
	try {
		$('[data-role="tagsinput"]').tagsinput({
			confirmKeys: [32]
		});
	} catch (e) {

	}

	$('input,textarea').on('keyup keypress', function (e) {
		var keyCode = e.keyCode || e.which;
		if (keyCode === 13) {
			e.preventDefault();
			return false;
		}
	});
});
$(document).ready(function () {
	toastr.options = {
		"closeButton": true,
		"debug": false,
		"newestOnTop": true,
		"progressBar": true,
		"positionClass": "toast-top-left",
		"preventDuplicates": false,
		"onclick": null,
		"showDuration": "500",
		"hideDuration": "1000",
		"timeOut": "7000",
		"extendedTimeOut": "1000",
		"showEasing": "swing",
		"hideEasing": "linear",
		"showMethod": "fadeIn",
		"hideMethod": "fadeOut"
	}
});