function PaiAjaxForm(selector, callback) {
	var $crudProgress = $("#crud_progress div");
	var clear = function () {
		if ($crudProgress) {
			$crudProgress.css("width", "0%");
			$crudProgress.text("0%");
		}
	}
	$(selector).ajaxForm({
		beforeSubmit: function (formData, jqForm, options) {

		},
		beforeSerialize: function () {
			try {
				for (instance in CKEDITOR.instances)
					CKEDITOR.instances[instance].updateElement();
			}
			catch (e) {

			}
		},
		success: function (response) {
			if (response.Notification)
				toastr[response.Notification.Status](response.Notification.Message, response.Notification.Title);
			if (response.IsRedirect)
				setTimeout(() => { window.location.href = response.RedirectUrl }, 1000);
			if (callback) {
				callback();
			}
		},
		error: function () {

		},
		beforeSend: function () {
			clear();
		},
		uploadProgress: function (event, position, total, percentComplete) {
			if ($crudProgress) {
				$crudProgress.css("width", percentComplete + "%");
				$crudProgress.text(percentComplete + "%");
			}
		},
		complete: function (xhr) {
		}

	});
}

function PaiConfirm(options, callback) {
	options.Title = options.Title || 'Emin misiniz ?';
	options.Status = options.Status || 'warning';
	options.ConfirmText = options.ConfirmText || 'Eminim';
	options.CancelText = options.CancelText || 'Vazgeç';
	swal({
		title: options.Title,
		text: options.Message,
		type: options.Status,
		showCancelButton: true,
		confirmButtonColor: '#3085d6',
		cancelButtonColor: '#d33',
		cancelButtonText: options.CancelText,
		confirmButtonText: options.ConfirmText
	}).then((result) => {
		if (result.value) {
			if (callback)
				callback();
		}
	})
}

function PaiDataTable(selector, options) {
	options.Order = options.Order || [[0, "desc"]];
	options.Columns = options.Columns || [];
	options.Columns.push({
		"mRender": function (data, type, row) {
			return "<a data-id='" + row.ID + "' class='btn btn-info btn-edit'><i class='fa fa-edit'></i></a>\
			<a data-id='" + row.ID + "' class='btn btn-danger btn-remove'><i class='glyphicon glyphicon-trash'></i></a>";
		}
	});
	// options.Edit
	// options.Remove
	var table = $(selector).DataTable({
		"processing": true,
		"serverSide": true,
		"ajax": options.Url,
		"sServerMethod": "POST",
		"order": options.Order,
		"language": {
			"url": "/assets/json/datatable-tr.json"
		},
		"columns": options.Columns
	}).on("draw", function () {
		$(".btn-edit").on("click", function () {
			var dataId = $(this).attr("data-id");
			if (options.Edit) {
				options.Edit(dataId);
			}
		});
		$(".btn-remove").on("click", function () {
			var dataId = $(this).attr("data-id");
			if (options.RemoveUrl) {
				PaiConfirm({
					Message: 'Bu veriyi silmek istediğinize emin misiniz ?'
				},  function (response) {
					// Eğer confirm tıklandıysa buraya girer // misal eminim
					// dedi
					$.get(options.RemoveUrl + dataId, function (responseData) {
						toastr[responseData.Notification.Status](responseData.Notification.Message, responseData.Notification.Title);
						if(responseData.IsSuccess)
						{
							$("[data-id='" + dataId + "']").parent().parent().fadeOut(1000)
							setTimeout(function () {
								obj.ReDraw();
							}, 1000);
						}
						
					});
				});
			}
		});

	});

	var obj = {
		ReDraw: function () {
			table.draw()
		}
	}
	return obj;

}

PaiPiper = {
	DateTime: {
		Parse : function(value) {
			var milliseconds = parseInt(value);
			var result = "-";
			if (milliseconds) {
				var date = new Date(milliseconds);
				result = date.toLocaleDateString("tr") + " " + date.toLocaleTimeString("tr");
			}
			return result;
		}
	},
	Logic: {
		Slice: function(value) {
			return ('0' + value).slice(-2);
		}
	}
} 