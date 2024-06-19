import { useState } from "react";
import {useAuth} from "./AuthProvider.jsx";
import {NavLink} from "react-router-dom";

const Login = () => {
    const [input, setInput] = useState({
        username: "",
        password: "",
    });

    const auth = useAuth();
    const handleSubmitEvent = (e) => {
        e.preventDefault();
        console.log("username: ", input.username);
        console.log("password: ", input.password);
        if (input.username !== "" && input.password !== "") {
            auth.loginAction(input).then(r => console.log(r));
            return;
        }
        alert("please provide a valid input");
    };

    const handleInput = (e) => {
        const { name, value } = e.target;
        setInput((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    return (
        <>
        <form onSubmit={handleSubmitEvent}>
            <div className="form_control">
                <label htmlFor="username">Username:</label>
                <input
                    type="text"
                    id="username"
                    name="username"
                    placeholder="username"
                    aria-describedby="username"
                    aria-invalid="false"
                    onChange={handleInput}
                />
                <div id="username" className="sr-only">
                    your username should be more than 6 character
                </div>
            </div>
            <div className="form_control">
                <label htmlFor="password">Password:</label>
                <input
                    type="password"
                    id="password"
                    name="password"
                    aria-describedby="user-password"
                    aria-invalid="false"
                    onChange={handleInput}
                />
                <div id="user-password" className="sr-only">
                    your password should be more than 6 character
                </div>
            </div>
            <button className="btn-submit">Submit</button>
        </form>
        <div>
            <NavLink to={"/signup"}>Signup</NavLink>
        </div>
        </>
)
    ;
};

export default Login;
