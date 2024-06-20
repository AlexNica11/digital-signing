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
        <div className="card">
            <form className="center card-content" onSubmit={handleSubmitEvent}>
                <div className="s12 m6 input-field">
                    <input id="username"
                           type="text"
                           className="validate"
                           name="username"
                           aria-describedby="username"
                           aria-invalid="false"
                           placeholder=" "
                           onChange={handleInput}/>
                    <label htmlFor="username">Username</label>
                    <span className="supporting-text" data-error="wrong" data-success="right">Your username should be more than 6 character</span>
                </div>
                <div className="s12 m6 input-field">
                    <input id="password"
                           type="password"
                           className="validate"
                           name="password"
                           aria-describedby="password"
                           aria-invalid="false"
                           placeholder=" "
                           maxLength="20"
                           onChange={handleInput}/>
                    <label htmlFor="password">Password</label>
                </div>
                <button className="btn tonal btn-submit left">Submit</button>
            </form>
            <div className="right">
                <NavLink className="btn tonal" to={"/signup"}>Signup</NavLink>
            </div>
        </div>
    )
        ;
};

export default Login;
