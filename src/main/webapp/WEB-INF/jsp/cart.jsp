<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <title>Your Cart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">

<div class="container mt-5">
    <h2 class="mb-4">🛒 Your Cart</h2>

    <table class="table table-bordered bg-white">
        <thead class="table-dark">
        <tr>
            <th>Product</th>
            <th>Price</th>
            <th width="150">Quantity</th>
            <th>Total</th>
            <th width="100">Action</th>
        </tr>
        </thead>
        <tbody>

        <c:forEach var="item" items="${items}">
            <tr>
                <!-- Product Name -->
                <td>${item.product.name}</td>

                <!-- ✅ FIX 1: price -> sellPrice -->
                <td>$${item.product.sellPrice}</td>

                <!-- Quantity Update -->
                <td>
                    <form action="${pageContext.request.contextPath}/cart"
                          method="post"
                          class="d-flex">

                        <input type="hidden" name="action" value="update"/>
                        <input type="hidden" name="productId"
                               value="${item.product.id}"/>

                        <input type="number"
                               name="qty"
                               value="${item.quantity}"
                               min="1"
                               class="form-control me-2"/>

                        <button class="btn btn-primary btn-sm">
                            Update
                        </button>
                    </form>
                </td>

                <!-- ✅ FIX 2: BigDecimal safe multiply -->
                <td>
                    $${item.product.sellPrice * item.quantity}
                </td>

                <!-- Remove -->
                <td>
                    <form action="${pageContext.request.contextPath}/cart"
                          method="post">

                        <input type="hidden" name="action" value="remove"/>
                        <input type="hidden" name="productId"
                               value="${item.product.id}"/>

                        <button class="btn btn-danger btn-sm">
                            X
                        </button>
                    </form>
                </td>
            </tr>
        </c:forEach>

        </tbody>
    </table>

    <a href="${pageContext.request.contextPath}/catalog"
       class="btn btn-secondary">
        ← Continue Shopping
    </a>

</div>

</body>
</html>