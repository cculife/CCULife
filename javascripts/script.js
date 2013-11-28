$(function() {
	var blocks = $("*[id]");

	$("#menu").click(function(event) {
		$("nav ul").slideToggle("slow");
	});

	$("nav a").click(function(event) {
		if($("#menu").css("display") == "block") {
			$("nav ul").slideUp("fast");
		}
	});

	$("a[href^=#]").click(function(event){
         event.preventDefault();

         var dest = 0;
         var offset = $("header").height() + 20;
         if($(this.hash).offset().top > $(document).height()-$(window).height()) {

              dest = $(document).height() - $(window).height();

         } else {

              dest = $(this.hash).offset().top - offset;
         }

         $('html,body').animate({scrollTop:dest}, 750,'swing');
    });

    $(document).scroll(function(event) {
    	var scrollTop = document.documentElement.scrollTop || 
				    	document.body.scrollTop || 
				    	0;

		changeCurrentNav();

    	if (scrollTop > 20) {
    		$("header").addClass('float');
    	} else {
    		$("header").removeClass('float');
    	}
	});

    function changeCurrentNav() {
    	var scrollTop = document.documentElement.scrollTop || 
				    	document.body.scrollTop || 
				    	0;
		var headerHeight = $("header").height()
		var offset = headerHeight + 30;
		var i, current;

    	for (i = 0; i < blocks.length; i++) {
    			if(scrollTop + offset < $(blocks[i]).offset().top) {
    				break;
    			} 
    	};

    	if(i > 0) {
    		var query = "";
    		for(var j = i - 1; j >= 0; j--) query += ",nav a[href=#" + blocks[j].id + "]";
    		current = $(query.substring(1)); 
    		if (current.length > 0) {
    			$(".active").removeClass("active");
    			current.last().parent().addClass('active');
    		}
    	} else {
    		$(".active").removeClass("active");
    		$("nav li:first").addClass("active");
    	}
    }

});
