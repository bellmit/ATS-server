<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="./bootstrap/css/bootstrap.min.css" rel="stylesheet">
<link href="./jquery-ui/jquery-ui.min.css" rel="stylesheet">
<script src="./jquery/jquery-3.2.1.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./jquery-ui/jquery-ui.min.js"></script>
<link href="./css/styleDetail.css" rel="stylesheet" type="text/css" />
<title>Server Details</title>
<script>
	$(function() {
		var dialog;
		function doCheck(command) {
			dialog = $("#check-spartdb")
					.dialog(
							{
								autoOpen : false,
								height : 400,
								width : 350,
								title : command,
								modal : true,
								buttons : {
									"Confirm" : function() {
										var spa = $('input[type="checkbox"][name="spa"]:checked');
										var rtdb = $('input[type="checkbox"][name="rtdb"]:checked');
										var s = "[";
										var r = "[";
										//alert(spa.get(0).value);
										if (spa.length == 0 && rtdb.length == 0) {
											$("#reminder")
													.dialog(
															{
																open : function(
																		event,
																		ui) {
																	$(this)
																			.html(
																					"");
																	$(this)
																			.append(
																					"<p>RTDB or SPA was not be checked!</p>");
																}
															});

											return;
										}
										for (var i = 0; i < spa.length; i++) {
											s += ("\"" + spa.get(i).value + "\"");
											if (i != spa.length - 1) {
												s += ",";
											}
										}
										s += "]";
										for (var i = 0; i < rtdb.length; i++) {
											r += ("\"" + rtdb.get(i).value + "\"");
											if (i != rtdb.length - 1) {
												r += ",";
											}
										}
										r += "]";

										//alert(s);
										if (command == "Remove Build") {
											confirm(
													"removeBuild",
													"Confirm to remove these builds?",
													s, r);
										} else if (command == "Update Build") {
											confirm(
													"updateBuild",
													"Confirm to update these builds?",
													s, r);
										}

									},
									"Cancel" : function() {
										$("#check-spartdb").dialog("close");
									}
								}
							});
			$("#check-spartdb").removeClass("hide");
		}

		function confirm(command, info, spa, rtdb) {
			$("#reminder").dialog({
				modal : true,
				buttons : {
					"Confirm" : function() {
						$.post("./com.alucn.web.server/serverControlServlet", {
							serverIP : $('#serverIP').val(),
							command : command,
							spa : spa,
							rtdb : rtdb
						}, function(data) {
							$("#reminder").dialog({
								buttons : {
									"Confirm" : function() {
										$(this).dialog("close");
										self.location = 'serverInfo.jsp';
									}
								},
								open : function(event, ui) {
									$(this).html("");
									$(this).append(data);
								}
							});
							//alert(data);
						});
						$('input:checkbox').prop('checked', false);
						$(this).dialog("close");
						$("#check-spartdb").dialog("close");
					},
					"Cancel" : function() {
						$(this).dialog("close");
					}
				},
				open : function(event, ui) {
					$(this).html("");
					$(this).append("<p>" + info + "</p>");
				}
			});
		}
		
		
			function confirmCancelAll(condition, info) {
				$("#reminder").dialog({
					modal : true,
					buttons : {
						"Confirm" : function() {
							$.post("./cancel.do", {
								condition : condition,
							}, function(data) {
								$("#reminder").dialog({
									buttons : {
										"Confirm" : function() {
											$(this).dialog("close");
											window.location.reload();
										}
									},
									open : function(event, ui) {
										$(this).html("");
										$(this).append(data);
									}
								});

							});
							$(this).dialog("close");
							$("#check-spartdb").dialog("close");
						},
						"Cancel" : function() {
							$(this).dialog("close");
						}
					},
					open : function(event, ui) {
						$(this).html("");
						$(this).append("<p>" + info + "</p>");
					}
				});

			}
			
			
				function confirmRemoveServer(condition, info) {
					$("#reminder").dialog({
						modal : true,
						buttons : {
							"Confirm" : function() {
								$.post("./removeServerInfo.do", {
									condition : condition,
								}, function(data) {
									$("#reminder").dialog({
										buttons : {
											"Confirm" : function() {
												$(this).dialog("close");
												window.location.reload();
											}
										},
										open : function(event, ui) {
											$(this).html("");
											$(this).append(data);
										}
									});

								});
								$(this).dialog("close");
								$("#check-spartdb").dialog("close");
							},
							"Cancel" : function() {
								$(this).dialog("close");
							}
						},
						open : function(event, ui) {
							$(this).html("");
							$(this).append("<p>" + info + "</p>");
						}
					});

				}
		
	
		$("#back").click(function() {
			location.href="./getServerInfo.do";
		});

		$("#updateBuild").click(function() {
			doCheck("Update Build");
			dialog.dialog("open");
		});

		$("#removeBuild").click(function() {
			doCheck("Remove Build");
			dialog.dialog("open");
		});

		$("#removeServer").click(function() {
			var serverName = document.getElementById("featureid").value;
			confirmRemoveServer(serverName, "Confirm to remove this server?");
		});

		$("#cancelAll").click(function() {
			var serverName = document.getElementById("featureid").value;
			confirmCancelAll(serverName, "Confirm to cancel all running cases?");
		});

	});
</script>
</head>
<body style="background-color:#ECEFF3;">
	<div class="container">
		<div class="row clearfix">
			<div class="col-md-12 column detail">
				<table class="table">
					<tr>
						<td>Server:</td>
						<td><span class="subdetail" id="serverName">${info.lab.serverName}</span></td>
						<td>IP:</td>
						<td><span class="subdetail">${info.lab.serverIp}</span></td>
					</tr>
					<tr>
						<td>Status:</td>
						<td colspan="1"><span class="subdetail">${info.taskStatus.status}</span></td>
						<td colspan="2"><span class="subdetail">${info.taskStatus.runningCase}</span></td>
					</tr>
					<tr>
						<td>Release:</td>
						<td><span class="subdetail">${info.lab.serverRelease}</span></td>
						<td>Protocol:</td>
						<td><span class="subdetail">${info.lab.serverProtocol}</span></td>
					</tr>
					<tr>
						<td>SPA:</td>
						<td class="subdetail" colspan="3"><c:forEach
								items="${info.lab.serverSPA}" var="spa">
							${spa}
						</c:forEach></td>
					</tr>
					<tr>
						<td>RTDB:</td>
						<td class="subdetail" colspan="3"><c:forEach
								items="${info.lab.serverRTDB}" var="rtdb">
							${rtdb}
						</c:forEach></td>
					</tr>
					<tr>
						<td>MateServer:</td>
						<td class="subdetail" colspan="3">
						${info.lab.mateServer}
						</td>
					</tr>
					<tr>
						<td colspan="4" class="submit">
							<button class="btn btn-default" name="back" id="back">back</button>
							<button class="btn btn-default" name="cancelAll" id="cancelAll">Cancel
								All</button>
<!-- 							<button class="btn btn-default " name="updateBuild" -->
<!-- 								id="updateBuild">Update Build</button> -->
<!-- 							<button class="btn btn-default " name="removeBuild" -->
<!-- 								id="removeBuild">Remove Build</button> -->
							<button class="btn btn-default " name="removeServer"
								id="removeServer">Remove Server</button>
						</td>
					</tr>
				</table>

				<div id="check-spartdb" class="hide">
					<form role="form"
						action="com.alucn.web.server/serverControlServlet" method="post"
						id="spartdb" name="spartdb">
						<input name="serverIP" id="serverIP" type="hidden" value="" />
						<h4 class="col-md-12 column">SPA</h4>
						<div class="form-group col-md-6 column">
							<label class="checkbox-inline"> <input type="checkbox"
								name="spa" value="aSpa">
							</label>
						</div>
						<h4 class="col-md-12 column">RTDB</h4>
						<div class="form-group col-md-6 column">
							<label class="checkbox-inline"> <input type="checkbox"
								name="rtdb" value="aRtdb">
							</label>
						</div>
					</form>
				</div>
				<div id="reminder"></div>
			</div>
		</div>
	</div>
</body>
</html>